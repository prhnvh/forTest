package by.bntu.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Start extends AppCompatActivity {
    private Dialog dialog; // Диалоговое окно для подтверждения выхода

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start); // Устанавливаем макет экрана из activity_start.xml
    }

    // Обработчик нажатия на кнопку "Открыть калькулятор"
    public void openCalc(View view) {
        Intent calc = new Intent(this, MainActivity.class); // Создаем интент для перехода на активность "MainActivity"
        startActivity(calc); // Запускаем активность "MainActivity"
    }

    // Обработчик нажатия на кнопку "Выход"
    public void onClickExit(View view) {
        dialog = new Dialog(this); // Создаем диалоговое окно
        dialog.setContentView(R.layout.exit_dialog); // Устанавливаем макет для диалогового окна из exit_dialog.xml
        dialog.show(); // Показываем диалоговое окно
    }

    // Обработчик нажатия на кнопку "Да" в диалоговом окне
    public void onClickYesExit(View view) {
        dialog.cancel(); // Закрываем диалоговое окно
        this.finish(); // Завершаем текущую активность, что приводит к закрытию приложения
    }

    // Обработчик нажатия на кнопку "Нет" в диалоговом окне
    public void onClickNoExit(View view) {
        dialog.cancel(); // Закрываем диалоговое окно
    }
}
