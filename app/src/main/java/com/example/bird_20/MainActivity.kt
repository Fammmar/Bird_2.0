
package com.example.bird_20
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mybird.R
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var taskList: RecyclerView
    private lateinit var menuButton: ImageButton
    private lateinit var addButton: ImageButton
    private lateinit var todayTextView: TextView
    private lateinit var adapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private val channelId = "bird_channel"
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskList = findViewById(R.id.taskList)
        menuButton = findViewById(R.id.menuButton)
        addButton = findViewById(R.id.addButton)
        todayTextView = findViewById(R.id.todayTextView)

        updateDateTime()
        startDateTimeUpdater()
        createNotificationChannel()

        tasks.add(Task("Черепаху покормить", "Сб, 14 февр. 2026 г."))
        tasks.add(Task("Иск Письмом", null))

        adapter = TaskAdapter(tasks) { task ->
            showTaskDialog(task)
        }
        taskList.layoutManager = LinearLayoutManager(this)
        taskList.adapter = adapter

        menuButton.setOnClickListener { showMenuDialog() }
        addButton.setOnClickListener { showMainChoiceDialog() }
    }

    private fun showMainChoiceDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            setBackgroundColor(android.graphics.Color.parseColor("#122150"))
        }

        val addTaskBtn = Button(this).apply {
            text = "📋 Добавить задачу"
            textSize = 16f
            setPadding(16, 16, 16, 16)
            setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                (parent as? AlertDialog)?.dismiss()
                showAddTaskDialog()
            }
        }
        layout.addView(addTaskBtn)

        layout.addView(TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                16
            )
        })

        val doctorBtn = Button(this).apply {
            text = "🏥 Записаться к врачу"
            textSize = 16f
            setPadding(16, 16, 16, 16)
            setBackgroundColor(android.graphics.Color.parseColor("#9C27B0"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                (parent as? AlertDialog)?.dismiss()
                showDoctorAppointmentDialog()
            }
        }
        layout.addView(doctorBtn)

        AlertDialog.Builder(this)
            .setView(layout)
            .setNegativeButton("Отмена", null)
            .show()
    }

    // ========== ДИАЛОГ ДОБАВЛЕНИЯ ЗАДАЧИ ==========
    private fun showAddTaskDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            setBackgroundColor(android.graphics.Color.parseColor("#122150"))
        }

        // Заголовок
        val titleHint = TextView(this).apply {
            text = "Заголовок"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
        }
        layout.addView(titleHint)

        val titleInput = EditText(this).apply {
            hint = "Введите название задачи"
            setPadding(0, 8, 0, 16)
            background = null
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.parseColor("#B0BEC5"))
        }
        layout.addView(titleInput)

        // Разделитель
        layout.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
            )
            setBackgroundColor(android.graphics.Color.parseColor("#B0BEC5"))
        })

        layout.addView(TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                16
            )
        })

        // Дата
        val dateHint = TextView(this).apply {
            text = "Дата"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
        }
        layout.addView(dateHint)

        val dateText = TextView(this).apply {
            text = "Выберите дату"
            setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_today, 0, 0, 0)
            compoundDrawablePadding = 8
            setPadding(0, 8, 0, 8)
            setTextColor(android.graphics.Color.WHITE)

            setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                DatePickerDialog(
                    this@MainActivity,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear)
                        this.text = formattedDate
                    },
                    year, month, day
                ).show()
            }
        }
        layout.addView(dateText)

        // Время
        val timeHint = TextView(this).apply {
            text = "Время"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 16, 0, 8)
            setTextColor(android.graphics.Color.WHITE)
        }
        layout.addView(timeHint)

        val timeText = TextView(this).apply {
            text = "Выберите время"
            setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_my_calendar, 0, 0, 0)
            compoundDrawablePadding = 8
            setPadding(0, 8, 0, 8)
            setTextColor(android.graphics.Color.WHITE)

            setOnClickListener {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                TimePickerDialog(
                    this@MainActivity,
                    { _, hourOfDay, minuteOfHour ->
                        val formattedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour)
                        this.text = formattedTime
                    },
                    hour, minute, true
                ).show()
            }
        }
        layout.addView(timeText)

        // КНОПКА СОХРАНИТЬ
        val saveButton = Button(this).apply {
            text = "СОХРАНИТЬ"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
            setTextColor(android.graphics.Color.WHITE)
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 24 }
        }
        layout.addView(saveButton)

        // ========== ДОБАВЛЕНА КНОПКА ОТМЕНА ==========
        val cancelButton = TextView(this).apply {
            text = "Отмена"
            textSize = 14f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(android.graphics.Color.parseColor("#B0BEC5"))
            setPadding(0, 16, 0, 0)
        }
        layout.addView(cancelButton)

        val dialog = AlertDialog.Builder(this)
            .setView(layout)
            .show()

        // ========== НАСТРОЙКА КНОПКИ ОТМЕНА ==========
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            if (title.isNotEmpty()) {
                val date = if (dateText.text != "Выберите дату") dateText.text.toString() else null
                val time = if (timeText.text != "Выберите время") timeText.text.toString() else null
                val dateTime = when {
                    date != null && time != null -> "$date $time"
                    date != null -> date
                    time != null -> time
                    else -> null
                }

                tasks.add(0, Task(title, dateTime))
                adapter.notifyItemInserted(0)

                dialog.dismiss()

                Toast.makeText(this@MainActivity, "✅ Задача создана", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ========== ДИАЛОГ ЗАПИСИ К ВРАЧУ ==========
    private fun showDoctorAppointmentDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            setBackgroundColor(android.graphics.Color.parseColor("#122150"))
        }

        // Заголовок
        val titleText = TextView(this).apply {
            text = "Запись к врачу"
            textSize = 20f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 0, 0, 24)
        }
        layout.addView(titleText)

        // Вопрос "Что болит?"
        val questionText = TextView(this).apply {
            text = "Что болит?"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
        }
        layout.addView(questionText)

        // Поле для ввода симптомов
        val symptomInput = EditText(this).apply {
            hint = "Введите симптомы..."
            setHintTextColor(android.graphics.Color.parseColor("#B0BEC5"))
            setTextColor(android.graphics.Color.WHITE)
            background = null
            setPadding(0, 8, 0, 8)
        }
        layout.addView(symptomInput)

        // Разделитель
        layout.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
            )
            setBackgroundColor(android.graphics.Color.parseColor("#B0BEC5"))
            setPadding(0, 0, 0, 16)
        })

        // Список подсказок (AutoCompleteTextView)
        val autoComplete = AutoCompleteTextView(this).apply {
            visibility = View.GONE
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.parseColor("#B0BEC5"))
        }
        layout.addView(autoComplete)

        // TextView для отображения найденного врача
        val doctorInfoText = TextView(this).apply {
            text = ""
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            visibility = View.GONE
            setPadding(0, 16, 0, 8)
        }
        layout.addView(doctorInfoText)

        // Контейнер для выбора даты и времени (изначально скрыт)
        val dateTimeContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        layout.addView(dateTimeContainer)

        // Кнопка "Записаться" (изначально скрыта)
        val bookButton = Button(this).apply {
            text = "Записаться"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
            setTextColor(android.graphics.Color.WHITE)
            setPadding(16, 16, 16, 16)
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16 }
        }
        layout.addView(bookButton)

        // Кнопка отмены
        val cancelButton = TextView(this).apply {
            text = "Отмена"
            textSize = 14f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(android.graphics.Color.parseColor("#B0BEC5"))
            setPadding(0, 16, 0, 0)
        }
        layout.addView(cancelButton)

        val dialog = AlertDialog.Builder(this)
            .setView(layout)
            .show()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // ========== БАЗА ДАННЫХ СИМПТОМОВ И ВРАЧЕЙ ==========
        val symptomDoctorMap = mapOf(
            // Терапевт
            "кашель" to "Терапевт — первичная диагностика, анализы, направление к узким специалистам",
            "температура" to "Терапевт — первичная диагностика, анализы, направление к узким специалистам",
            "слабость" to "Терапевт — первичная диагностика, анализы, направление к узким специалистам",
            "озноб" to "Терапевт — первичная диагностика, анализы, направление к узким специалистам",
            "недомогание" to "Терапевт — первичная диагностика, анализы, направление к узким специалистам",
            "простуда" to "Терапевт — первичная диагностика, анализы, направление к узким специалистам",
            "грипп" to "Терапевт — первичная диагностика, анализы, направление к узким специалистам",
            "орви" to "Терапевт — первичная диагностика, анализы, направление к узким специалистам",

            // Гинеколог
            "беременность" to "Гинеколог — заболевания женской репродуктивной системы, планирование семьи, контрацепция",
            "месячные" to "Гинеколог — заболевания женской репродуктивной системы, планирование семьи, контрацепция",
            "менструация" to "Гинеколог — заболевания женской репродуктивной системы, планирование семьи, контрацепция",
            "цистит" to "Гинеколог — заболевания женской репродуктивной системы, планирование семьи, контрацепция",
            "выделения" to "Гинеколог — заболевания женской репродуктивной системы, планирование семьи, контрацепция",
            "климакс" to "Гинеколог — заболевания женской репродуктивной системы, планирование семьи, контрацепция",
            "менопауза" to "Гинеколог — заболевания женской репродуктивной системы, планирование семьи, контрацепция",

            // Уролог
            "почки" to "Уролог — заболевания мочевыделительной системы у мужчин и женщин",
            "мочевой" to "Уролог — заболевания мочевыделительной системы у мужчин и женщин",
            "простатит" to "Уролог — заболевания мочевыделительной системы у мужчин и женщин",
            "аденома" to "Уролог — заболевания мочевыделительной системы у мужчин и женщин",

            // Кардиолог
            "сердце" to "Кардиолог — лечение заболеваний сердца и сосудов: гипертония, аритмии, сердечная недостаточность",
            "давление" to "Кардиолог — лечение заболеваний сердца и сосудов: гипертония, аритмии, сердечная недостаточность",
            "гипертония" to "Кардиолог — лечение заболеваний сердца и сосудов: гипертония, аритмии, сердечная недостаточность",
            "аритмия" to "Кардиолог — лечение заболеваний сердца и сосудов: гипертония, аритмии, сердечная недостаточность",
            "тахикардия" to "Кардиолог — лечение заболеваний сердца и сосудов: гипертония, аритмии, сердечная недостаточность",
            "одышка" to "Кардиолог — лечение заболеваний сердца и сосудов: гипертония, аритмии, сердечная недостаточность",
            "груди боль" to "Кардиолог — лечение заболеваний сердца и сосудов: гипертония, аритмии, сердечная недостаточность",

            // Невролог
            "голова" to "Невролог — заболевания нервной системы: мигрени, радикулит, инсульт",
            "мигрень" to "Невролог — заболевания нервной системы: мигрени, радикулит, инсульт",
            "головная боль" to "Невролог — заболевания нервной системы: мигрени, радикулит, инсульт",
            "радикулит" to "Невролог — заболевания нервной системы: мигрени, радикулит, инсульт",
            "инсульт" to "Невролог — заболевания нервной системы: мигрени, радикулит, инсульт",
            "спина" to "Невролог — заболевания нервной системы: мигрени, радикулит, инсульт",
            "онемение" to "Невролог — заболевания нервной системы: мигрени, радикулит, инсульт",

            // Эндокринолог
            "щитовидка" to "Эндокринолог — гормональные нарушения, заболевания щитовидной железы, сахарный диабет",
            "диабет" to "Эндокринолог — гормональные нарушения, заболевания щитовидной железы, сахарный диабет",
            "сахар" to "Эндокринолог — гормональные нарушения, заболевания щитовидной железы, сахарный диабет",
            "гормоны" to "Эндокринолог — гормональные нарушения, заболевания щитовидной железы, сахарный диабет",
            "вес" to "Эндокринолог — гормональные нарушения, заболевания щитовидной железы, сахарный диабет",

            // Гастроэнтеролог
            "живот" to "Гастроэнтеролог — заболевания желудочно-кишечного тракта",
            "желудок" to "Гастроэнтеролог — заболевания желудочно-кишечного тракта",
            "гастрит" to "Гастроэнтеролог — заболевания желудочно-кишечного тракта",
            "язва" to "Гастроэнтеролог — заболевания желудочно-кишечного тракта",
            "печень" to "Гастроэнтеролог — заболевания желудочно-кишечного тракта",
            "поджелудочная" to "Гастроэнтеролог — заболевания желудочно-кишечного тракта",
            "кишечник" to "Гастроэнтеролог — заболевания желудочно-кишечного тракта",
            "изжога" to "Гастроэнтеролог — заболевания желудочно-кишечного тракта",
            "тошнота" to "Гастроэнтеролог — заболевания желудочно-кишечного тракта",

            // ЛОР
            "ухо" to "ЛОР (отоларинголог) — заболевания уха, горла и носа: ринит, синусит, отит",
            "горло" to "ЛОР (отоларинголог) — заболевания уха, горла и носа: ринит, синусит, отит",
            "нос" to "ЛОР (отоларинголог) — заболевания уха, горла и носа: ринит, синусит, отит",
            "отит" to "ЛОР (отоларинголог) — заболевания уха, горла и носа: ринит, синусит, отит",
            "синусит" to "ЛОР (отоларинголог) — заболевания уха, горла и носа: ринит, синусит, отит",
            "насморк" to "ЛОР (отоларинголог) — заболевания уха, горла и носа: ринит, синусит, отит",
            "гланды" to "ЛОР (отоларинголог) — заболевания уха, горла и носа: ринит, синусит, отит",

            // Офтальмолог
            "глаза" to "Офтальмолог — лечение заболеваний глаз: снижение остроты зрения, дискомфорт",
            "зрение" to "Офтальмолог — лечение заболеваний глаз: снижение остроты зрения, дискомфорт",
            "очки" to "Офтальмолог — лечение заболеваний глаз: снижение остроты зрения, дискомфорт",
            "конъюнктивит" to "Офтальмолог — лечение заболеваний глаз: снижение остроты зрения, дискомфорт",

            // Ортопед-травматолог
            "сустав" to "Ортопед-травматолог — травмы, боли в суставах и спине, нарушения опорно-двигательного аппарата",
            "травма" to "Ортопед-травматолог — травмы, боли в суставах и спине, нарушения опорно-двигательного аппарата",
            "перелом" to "Ортопед-травматолог — травмы, боли в суставах и спине, нарушения опорно-двигательного аппарата",
            "осанка" to "Ортопед-травматолог — травмы, боли в суставах и спине, нарушения опорно-двигательного аппарата",
            "сколиоз" to "Ортопед-травматолог — травмы, боли в суставах и спине, нарушения опорно-двигательного аппарата",

            // Мануальный терапевт
            "шея" to "Мануальный терапевт — лечение заболеваний опорно-двигательного аппарата ручными методиками",
            "спина" to "Мануальный терапевт — лечение заболеваний опорно-двигательного аппарата ручными методиками",
            "позвоночник" to "Мануальный терапевт — лечение заболеваний опорно-двигательного аппарата ручными методиками",
            "остеохондроз" to "Мануальный терапевт — лечение заболеваний опорно-двигательного аппарата ручными методиками",

            // Дерматолог
            "кожа" to "Дерматолог — лечение кожных заболеваний: акне, экзема, псориаз",
            "прыщи" to "Дерматолог — лечение кожных заболеваний: акне, экзема, псориаз",
            "акне" to "Дерматолог — лечение кожных заболеваний: акне, экзема, псориаз",
            "экзема" to "Дерматолог — лечение кожных заболеваний: акне, экзема, псориаз",
            "псориаз" to "Дерматолог — лечение кожных заболеваний: акне, экзема, псориаз",
            "сыпь" to "Дерматолог — лечение кожных заболеваний: акне, экзема, псориаз",
            "аллергия" to "Дерматолог — лечение кожных заболеваний: акне, экзема, псориаз"
        )

        // Список всех симптомов для подсказок
        val allSymptoms = symptomDoctorMap.keys.toList()

        // Настройка AutoCompleteTextView
        val autoCompleteAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, allSymptoms)
        autoComplete.setAdapter(autoCompleteAdapter)
        autoComplete.threshold = 2

        // Слушатель изменения текста
        symptomInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase(Locale.getDefault())
                if (query.length >= 2) {
                    autoComplete.setText("")
                    autoComplete.visibility = View.VISIBLE

                    val filteredSymptoms = allSymptoms.filter {
                        it.contains(query, ignoreCase = true)
                    }

                    if (filteredSymptoms.isNotEmpty()) {
                        val filteredAdapter = ArrayAdapter(this@MainActivity,
                            android.R.layout.simple_dropdown_item_1line, filteredSymptoms)
                        autoComplete.setAdapter(filteredAdapter)
                        autoComplete.showDropDown()
                    } else {
                        autoComplete.dismissDropDown()
                    }
                } else {
                    autoComplete.visibility = View.GONE
                    autoComplete.dismissDropDown()
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Обработка выбора подсказки
        autoComplete.setOnItemClickListener { parent, _, position, _ ->
            val selectedSymptom = parent.getItemAtPosition(position).toString()
            symptomInput.setText(selectedSymptom)
            symptomInput.setSelection(selectedSymptom.length)
            autoComplete.visibility = View.GONE

            val doctorInfo = symptomDoctorMap[selectedSymptom.lowercase(Locale.getDefault())]

            if (doctorInfo != null) {
                doctorInfoText.text = "✅ Найден врач: $doctorInfo"
                doctorInfoText.visibility = View.VISIBLE
                bookButton.visibility = View.VISIBLE
                dateTimeContainer.visibility = View.GONE
            } else {
                doctorInfoText.text = "❌ Совпадений не найдено"
                doctorInfoText.visibility = View.VISIBLE
                bookButton.visibility = View.GONE
                dateTimeContainer.visibility = View.GONE
            }
        }

        // Обработка кнопки "Записаться"
        bookButton.setOnClickListener {
            bookButton.visibility = View.GONE
            doctorInfoText.visibility = View.GONE
            dateTimeContainer.visibility = View.VISIBLE

            dateTimeContainer.removeAllViews()

            val dateTimeTitle = TextView(this@MainActivity).apply {
                text = "Выберите дату и время приема"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.WHITE)
                setPadding(0, 16, 0, 16)
            }
            dateTimeContainer.addView(dateTimeTitle)

            val dateText = TextView(this@MainActivity).apply {
                text = "Дата не выбрана"
                setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_today, 0, 0, 0)
                compoundDrawablePadding = 8
                setPadding(0, 8, 0, 8)
                setTextColor(android.graphics.Color.WHITE)

                setOnClickListener {
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(
                        this@MainActivity,
                        { _, year, month, day ->
                            val selectedDate = String.format("%02d.%02d.%d", day, month + 1, year)
                            this.text = selectedDate
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            }
            dateTimeContainer.addView(dateText)

            val timeText = TextView(this@MainActivity).apply {
                text = "Время не выбрано"
                setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_my_calendar, 0, 0, 0)
                compoundDrawablePadding = 8
                setPadding(0, 8, 0, 16)
                setTextColor(android.graphics.Color.WHITE)

                setOnClickListener {
                    val calendar = Calendar.getInstance()
                    TimePickerDialog(
                        this@MainActivity,
                        { _, hour, minute ->
                            val selectedTime = String.format("%02d:%02d", hour, minute)
                            this.text = selectedTime
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                }
            }
            dateTimeContainer.addView(timeText)

            val reminderTitle = TextView(this@MainActivity).apply {
                text = "Оповестить о приеме за:"
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.WHITE)
                setPadding(0, 16, 0, 8)
            }
            dateTimeContainer.addView(reminderTitle)

            val reminderSpinner = Spinner(this@MainActivity).apply {
                val reminders = arrayOf("10 минут", "15 минут", "30 минут", "1 час", "2 часа", "1 день")
                adapter = ArrayAdapter(this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item, reminders)
            }
            dateTimeContainer.addView(reminderSpinner)

            val confirmButton = Button(this@MainActivity).apply {
                text = "ПОДТВЕРДИТЬ ЗАПИСЬ"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                setTextColor(android.graphics.Color.WHITE)
                setPadding(16, 16, 16, 16)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 24 }

                setOnClickListener {
                    val doctorName = doctorInfoText.text.toString().replace("✅ Найден врач: ", "")
                    val date = if (dateText.text != "Дата не выбрана") dateText.text.toString() else "13.02.2026"
                    val time = if (timeText.text != "Время не выбрано") timeText.text.toString() else "14:30"
                    val reminder = reminderSpinner.selectedItem.toString()

                    tasks.add(0, Task(
                        title = "Приём у ${doctorName.substringBefore(" —")}",
                        date = "$date $time",
                        doctorName = doctorName,
                        symptom = symptomInput.text.toString()
                    ))
                    adapter.notifyItemInserted(0)

                    Toast.makeText(this@MainActivity, "✅ Запись создана. Уведомление за $reminder", Toast.LENGTH_LONG).show()

                    // ===== ОТКРЫВАЕМ САЙТ =====
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://burdenko.ru/general-information"))
                    startActivity(intent)

                    dialog.dismiss()
                }
            }
            dateTimeContainer.addView(confirmButton)

            val backButton = TextView(this@MainActivity).apply {
                text = "Назад"
                textSize = 14f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setTextColor(android.graphics.Color.parseColor("#B0BEC5"))
                setPadding(0, 16, 0, 0)

                setOnClickListener {
                    dateTimeContainer.visibility = View.GONE
                    bookButton.visibility = View.VISIBLE
                    doctorInfoText.visibility = View.VISIBLE
                }
            }
            dateTimeContainer.addView(backButton)
        }
    }

    // Обновление времени
    private fun updateDateTime() {
        try {
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale("ru"))
            val currentDateTime = dateFormat.format(Date())
            todayTextView.text = " Сегодня у нас: $currentDateTime"
        } catch (e: Exception) {
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale.ENGLISH)
            val currentDateTime = dateFormat.format(Date())
            todayTextView.text = " Today: $currentDateTime"
        }
    }

    private fun startDateTimeUpdater() {
        runnable = object : Runnable {
            override fun run() {
                updateDateTime()
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(runnable!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        runnable?.let { handler.removeCallbacks(it) }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Bird уведомления",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showTaskDialog(task: Task) {
        AlertDialog.Builder(this)
            .setTitle(task.title)
            .setMessage("Срок: ${task.date ?: "Нет даты"}")
            .setPositiveButton("OK", null)
            .setNeutralButton("Удалить") { _, _ ->
                tasks.remove(task)
                adapter.notifyDataSetChanged()
            }
            .show()
    }

    private fun showMenuDialog() {
        val items = arrayOf(
            "Списки задач",
            "Добавить несколько задач",
            "Удалить объявления",
            "Другие приложения",
            "Отправить отзыв",
            "Следуйте за нами",
            "Пригласите друзей в приложение",
            "Настройки"
        )
        AlertDialog.Builder(this)
            .setTitle("Меню")
            .setItems(items) { dialog, which ->
                if (items[which] == "Другие приложения") {
                    showDoctorAppointmentDialog()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}

// Модель данных задачи
data class Task(
    val title: String,
    val date: String?,
    val doctorName: String? = null,
    val symptom: String? = null,
    var isCompleted: Boolean = false
)


// Адаптер для RecyclerView
// Адаптер для RecyclerView
class TaskAdapter(
    private val tasks: List<Task>,
    private val onClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val dateTimeText: TextView = itemView.findViewById(R.id.dateTimeText) // ИСПРАВЛЕНО!
        val reminderSpinner: Spinner = itemView.findViewById(R.id.reminderSpinner)
        val timeRemainingText: TextView = itemView.findViewById(R.id.timeRemainingText)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]

        // Основная информация
        holder.titleText.text = task.title
        holder.dateTimeText.text = task.date ?: "Нет даты" // ИСПОЛЬЗУЕМ dateTimeText
        holder.checkbox.isChecked = task.isCompleted

        // Настройка выпадающего списка
        val reminders = arrayOf("за день", "на неделю", "на 2 часа", "за час", "за 30 минут")
        val spinnerAdapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_dropdown_item,
            reminders
        )
        holder.reminderSpinner.adapter = spinnerAdapter

        // Расчет оставшегося времени (упрощенная версия)
        if (task.date != null && task.date != "Нет даты") {
            holder.timeRemainingText.text = "Осталось: 2 дня"
            holder.progressBar.progress = 60
        } else {
            holder.timeRemainingText.text = "Дата не указана"
            holder.progressBar.visibility = View.GONE
        }

        // Обработка чекбокса
        holder.checkbox.setOnClickListener {
            task.isCompleted = !task.isCompleted
            notifyItemChanged(position)
        }

        // Клик по задаче
        holder.itemView.setOnClickListener { onClick(task) }
    }

    override fun getItemCount() = tasks.size
}



/*

package com.example.bird_20
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mybird.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var taskList: RecyclerView
    private lateinit var menuButton: ImageButton
    private lateinit var addButton: ImageButton
    private lateinit var todayTextView: TextView
    private lateinit var adapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private val channelId = "bird_channel"
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskList = findViewById(R.id.taskList)
        menuButton = findViewById(R.id.menuButton)
        addButton = findViewById(R.id.addButton)
        todayTextView = findViewById(R.id.todayTextView)

        updateDateTime()
        startDateTimeUpdater()
        createNotificationChannel()

        adapter = TaskAdapter(tasks) { task ->
            showTaskDialog(task)
        }
        taskList.layoutManager = LinearLayoutManager(this)
        taskList.adapter = adapter

        menuButton.setOnClickListener { showMenuDialog() }
        addButton.setOnClickListener { showMainChoiceDialog() }
    }

    private fun showMainChoiceDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            setBackgroundColor(android.graphics.Color.parseColor("#122150"))
        }

        val addTaskBtn = Button(this).apply {
            text = "📋 Добавить задачу"
            textSize = 16f
            setPadding(16, 16, 16, 16)
            setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                (parent as? AlertDialog)?.dismiss()
                showAddTaskDialog()
            }
        }
        layout.addView(addTaskBtn)

        layout.addView(TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                16
            )
        })

        val doctorBtn = Button(this).apply {
            text = "🏥 Записаться к врачу"
            textSize = 16f
            setPadding(16, 16, 16, 16)
            setBackgroundColor(android.graphics.Color.parseColor("#9C27B0"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                (parent as? AlertDialog)?.dismiss()
                showDoctorAppointmentDialog()
            }
        }
        layout.addView(doctorBtn)

        AlertDialog.Builder(this)
            .setView(layout)
            .setNegativeButton("Отмена", null)
            .show()
    }

    // ========== ДИАЛОГ ДОБАВЛЕНИЯ ЗАДАЧИ ==========
    private fun showAddTaskDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            setBackgroundColor(android.graphics.Color.parseColor("#122150"))
        }

        // Заголовок
        val titleHint = TextView(this).apply {
            text = "Заголовок"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
        }
        layout.addView(titleHint)

        val titleInput = EditText(this).apply {
            hint = "Введите название задачи"
            setPadding(0, 8, 0, 16)
            background = null
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.parseColor("#B0BEC5"))
        }
        layout.addView(titleInput)

        // Разделитель
        layout.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
            )
            setBackgroundColor(android.graphics.Color.parseColor("#B0BEC5"))
        })

        layout.addView(TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                16
            )
        })

        // Дата
        val dateHint = TextView(this).apply {
            text = "Дата"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
        }
        layout.addView(dateHint)

        val dateText = TextView(this).apply {
            text = "Выберите дату"
            setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_today, 0, 0, 0)
            compoundDrawablePadding = 8
            setPadding(0, 8, 0, 8)
            setTextColor(android.graphics.Color.WHITE)

            setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                DatePickerDialog(
                    this@MainActivity,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear)
                        this.text = formattedDate
                    },
                    year, month, day
                ).show()
            }
        }
        layout.addView(dateText)

        // Время
        val timeHint = TextView(this).apply {
            text = "Время"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 16, 0, 8)
            setTextColor(android.graphics.Color.WHITE)
        }
        layout.addView(timeHint)

        val timeText = TextView(this).apply {
            text = "Выберите время"
            setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_my_calendar, 0, 0, 0)
            compoundDrawablePadding = 8
            setPadding(0, 8, 0, 8)
            setTextColor(android.graphics.Color.WHITE)

            setOnClickListener {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                TimePickerDialog(
                    this@MainActivity,
                    { _, hourOfDay, minuteOfHour ->
                        val formattedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour)
                        this.text = formattedTime
                    },
                    hour, minute, true
                ).show()
            }
        }
        layout.addView(timeText)

        // КНОПКА СОХРАНИТЬ
        val saveButton = Button(this).apply {
            text = "СОХРАНИТЬ"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
            setTextColor(android.graphics.Color.WHITE)
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 24 }
        }
        layout.addView(saveButton)

        // КНОПКА ОТМЕНА
        val cancelButton = TextView(this).apply {
            text = "Отмена"
            textSize = 14f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(android.graphics.Color.parseColor("#B0BEC5"))
            setPadding(0, 16, 0, 0)
        }
        layout.addView(cancelButton)

        val dialog = AlertDialog.Builder(this)
            .setView(layout)
            .show()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            if (title.isNotEmpty()) {
                val date = if (dateText.text != "Выберите дату") dateText.text.toString() else null
                val time = if (timeText.text != "Выберите время") timeText.text.toString() else null
                val dateTime = when {
                    date != null && time != null -> "$date $time"
                    date != null -> date
                    time != null -> time
                    else -> null
                }

                tasks.add(0, Task(title, dateTime))
                adapter.notifyItemInserted(0)

                dialog.dismiss()

                Toast.makeText(this@MainActivity, "✅ Задача создана", Toast.LENGTH_SHORT).show()
            }
        }
    }
*/