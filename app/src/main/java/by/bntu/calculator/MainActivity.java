package by.bntu.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public Calculator calculator;
    private Animation animation1, animation2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Загружаем анимации
        animation1 = AnimationUtils.loadAnimation(this, R.anim.rotate);
        animation2 = AnimationUtils.loadAnimation(this, R.anim.moving);
        // Находим элементы TextView для ввода и вывода данных
        TextView inputView = findViewById(R.id.calcInputText);
        inputView.startAnimation(animation2); // // Запускаем анимацию для inputView
        TextView outputView = findViewById(R.id.calcResultText);

        // Инициализируем объект калькулятора, передавая ему TextView для ввода и вывода
        calculator = new Calculator(this, inputView, outputView);
    }

    // Обработчик нажатия на кнопки калькулятора
    public void handleBtnPress(View view) {
        Button btn = (Button) view;
        String inputData = (String) btn.getText();
        calculator.handleInput(inputData);
        //btn.startAnimation(animation1);
    }

    // Метод для очистки ввода калькулятора
    public void clearCalcInput(View view) {

        view.startAnimation(animation1);  // Запускаем анимацию для кнопки "Очистить"
        calculator.clearCalculatorInput();
    }

    // Метод для удаления последнего символа из ввода
    public void deleteLastInputChar(View view) {
        calculator.deleteLastInputChar();
    }

    // Метод для открытия инженерного калькулятора
    public void openEngineerCalc(View view) {
        ArrayList<Button> btns = new ArrayList<Button>();
        btns.add(findViewById(R.id.btnSin));
        btns.add(findViewById(R.id.btnCos));
        btns.add(findViewById(R.id.btnSquareRoot));
        btns.add(findViewById(R.id.btnLog));

        // Показываем инженерные кнопки с анимацией
        calculator.showEngineerCalc(btns);
    }

}