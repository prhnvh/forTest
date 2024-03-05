package by.bntu.calculator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Calculator {
    // Поля класса
    private final TextView inputView; // Поле для ввода данных
    private final TextView outputView; // Поле для вывода результата
    private final Context context; // Контекст приложения

    // Индексы и значения для обработки операций и чисел
    private int lastOperandIndex = 0;
    private int oldOperandIndex = 0;
    private final float PI = 3.14159265359F;
    private final float E = 2.71828182846F;
    private final String PI_Code = "\uD835\uDF45";
    private final String Sqrt_Code = "√";

    // Последняя введенная операция и значения
    private Operations lastInputOperation = Operations.Plus;
    private float lastInputValue = 0;
    private float lastResult = 0;
    private boolean operationAfterNumberWas = false;

    // if we got Infinity or NaN
    // Флаг для очистки поля ввода при новом вводе
    private boolean clearInputViewOnNewInput = false;

    // Списки символов и команд
    private ArrayList<Character> deleteDefaultOperationSpecialCases = new ArrayList<Character>();
    private ArrayList<String> engineerCommands = new ArrayList<String>();

    // Списки для чисел и операций
    private final ArrayList<Float> numbersByItsOrder = new ArrayList<>();
    private final ArrayList<Operations> operationsInARow = new ArrayList<>();

    // Конструктор класса Calculator
    public Calculator(Context context, TextView inputView, TextView outputView) {
        this.inputView = inputView;
        this.outputView = outputView;
        this.context = context;

        initDeleteCases(); // Инициализация списка символов для удаления
        initCommands(); // Инициализация списка команд инженерного калькулятора
    }

    // Метод для обработки ввода данных
    public void handleInput(String inputData) {
        // Если флаг очистки активен, очищаем поле ввода
        if (clearInputViewOnNewInput) {
            clearCalculatorInput();
            clearInputViewOnNewInput = false;
        }

        try {
            // Если введенное значение - число, добавляем его
            if (inputView.length() == 1 && inputView.getText().charAt(0) == '0' ||
                    inputView.getText() == "0.0") {
                inputView.setText("");
            }
            int number = Integer.parseInt(inputData);
            appendInputNumberDigit(number);
        } catch (NumberFormatException e) {
            int inputTextSize = inputView.getText().length();

            if (handleEngineerOperation(inputData)) {
                return;
            }

            // Если введен символ +, -, /, *, . или =, обрабатываем операцию
            // if it's + - / * . =
            if (inputData.length() == 1 && !Character.isDigit(inputData.charAt(0))) {

                Character inputChar = inputData.charAt(0);

                if (inputChar != '.' && inputTextSize > 0) {
                    // check last symbol we have already input
                    char lastInputSymbol = inputView.getText().charAt(inputView.length() - 1);
                    if (lastInputSymbol == '.') {
                        inputView.append("0");
                    } else if (!Character.isDigit(lastInputSymbol)) {
                        // remove + / * -
                        inputView.setText(inputView.getText().subSequence(0, inputView.length() - 1));
                        inputTextSize = inputView.getText().length();
                        numbersByItsOrder.remove(numbersByItsOrder.size() - 1);
                        operationsInARow.remove(operationsInARow.size() - 1);
                        lastOperandIndex = oldOperandIndex;
                    }
                }

                // check for new input data
                if (inputChar == '-' && inputTextSize == 0) {
                    numbersByItsOrder.add(0F);
                    operationsInARow.add(0, Operations.Minus);
                    inputView.append(inputData);
                    return;
                }

                // try to add point to number
                else if (inputChar == '.') {

                    int pointsCount = 0;
                    int textLen = inputView.getText().length();
                    if (lastOperandIndex >= textLen - 1 && textLen != 1) {
                        inputView.append("0");
                    }
                    CharSequence inputText = inputView.getText();

                    for (int i = lastOperandIndex; i < inputTextSize; i++) {
                        if (inputText.charAt(i) == '.') {
                            // we have already point in number so return
                            return;
                        }
                    }
                    inputView.append(inputData);
                    return;
                }

                if (!parseLastNumberValue()) {
                    if (inputView.getText().length() == 0) {
                        inputView.append("0.0");
                    }
                    return;
                }
            }

            if (inputData.equals("=")) {
                lastResult = calculateExpression();
                outputView.setText(inputView.getText());
                String resultText = Float.toString(lastResult);
                inputView.setText(resultText);

                // clear input then
                if (resultText.equals("Infinity") || resultText.equals("NaN")) {
                    clearInputViewOnNewInput = true;
                }

                if (operationsInARow.size() != 0 && operationAfterNumberWas) {
                    lastInputValue = numbersByItsOrder.get(numbersByItsOrder.size() - 1);
                }

                clearCalculatorData();

                // if we got negative result -> add 0 - operation
                if (lastResult < 0) {
                    numbersByItsOrder.add(0F);
                    operationsInARow.add(0, Operations.Minus);
                }
            } else {
                appendInputOperand(inputData);
            }
        }
    }

    // Если введен символ +, -, /, *, . или =, обрабатываем операцию
    public void deleteLastInputChar() {
        if ("0".contentEquals(inputView.getText())) {
            return;
        }

        int lastInputIndex = inputView.length() - 1;
        if (lastInputIndex == 0) {
            numbersByItsOrder.clear();
            operationsInARow.clear();
            lastOperandIndex = 0;
            inputView.setText("0");
            lastInputOperation = Operations.Plus;
            lastInputValue = 0;
            return;
        }

        Character deletedSymbol = inputView.getText().charAt(lastInputIndex);
        inputView.setText(inputView.getText().subSequence(0, lastInputIndex));

        if (deleteDefaultOperationSpecialCases.contains(deletedSymbol)) {
            if (numbersByItsOrder.size() > 1) {
                numbersByItsOrder.remove(numbersByItsOrder.size() - 1);
            }
            operationsInARow.remove(operationsInARow.size() - 1);
            lastOperandIndex = getAvailableLastOperandIndex();
            lastInputOperation = parseOperation(Character.toString(inputView.getText().charAt(lastOperandIndex)));
            lastInputValue = 0;
        }
    }

    // Метод для очистки поля ввода
    public void clearCalculatorInput() {
        clearCalculatorData();
        inputView.setText("0");
        outputView.setText("0");
        lastResult = 0;
        lastInputValue = 0;
        lastInputOperation = Operations.Plus;
    }

    // Метод для анимированного показа или скрытия инженерных кнопок
    public void showEngineerCalc(ArrayList<Button> btns) {
        float scaleValue = 1f;
        boolean flag = true;
        if (btns.get(0).getScaleX() == 1f) {
            scaleValue = 0f;
            flag = false;
        }

        AnimatorSet set = new AnimatorSet();
        set.setDuration(700);
        set.setInterpolator(new DecelerateInterpolator());
        // Проходим по списку кнопок и анимируем их изменение размера
        for (Button item : btns) {
            item.setEnabled(flag);
            ObjectAnimator animationX = ObjectAnimator.ofFloat(item, "scaleX", scaleValue);
            ObjectAnimator animationY = ObjectAnimator.ofFloat(item, "scaleY", scaleValue);

            set.play(animationX).with(animationY);
            set.start();
        }
    }

    // Метод для очистки данных калькулятора
    private void clearCalculatorData() {
        operationsInARow.clear();
        numbersByItsOrder.clear();
        lastOperandIndex = 0;
        oldOperandIndex = 0;
        operationAfterNumberWas = false;
    }

    // Метод для добавления оператора в поле ввода
    private void appendInputOperand(String symbol) {
        Operations enteredOperation = parseOperation(symbol);
        if (enteredOperation != Operations.None) {
            inputView.append(symbol);
            operationsInARow.add(enteredOperation);
            lastInputOperation = enteredOperation;
            operationAfterNumberWas = true;
        }
    }

    // Метод для добавления цифры в поле ввода
    private void appendInputNumberDigit(int digit) {
        inputView.append(Integer.toString(digit));
    }

    // Метод для анализа последнего введенного числа
    // Возвращает true, если успешно распарсили последнее число
    // returns true on successful parse
    private boolean parseLastNumberValue() {
        String lastNumStringResult = getLastNumbString();
        if (lastNumStringResult.equals("")) {
            return false;
        } else if (lastNumStringResult.equals("=")) {
            return true;
        }

        float number;
        try {
            number = Float.parseFloat(lastNumStringResult);
        } catch (Exception e) {
            Toast.makeText(context, "Bad Number input, try again!", Toast.LENGTH_LONG).show();
            return false;
        }

        numbersByItsOrder.add(number);
        oldOperandIndex = lastOperandIndex;
        lastOperandIndex = inputView.getText().length();
        return true;
    }

    // Метод для замены последнего введенного числа
    private void swapLastNumber(String toNumberString) {
        int inputTextSize = inputView.getText().length();
        if (inputTextSize == 1 && inputView.getText().charAt(0) == '-') {
            toNumberString = "-" + toNumberString;
        }
        // Удаляем предыдущие числа и заменяем их новым числом
        // delete numbers
        inputView.setText(inputView.getText().subSequence(0, lastOperandIndex == 0 ?
                0 : lastOperandIndex + 1));
        inputView.append(toNumberString);
    }

    // Метод для получения строки с последним введенным числом
    private String getLastNumbString() {
        CharSequence charSequence = inputView.getText();
        int totalTextSize = charSequence.length();
        if (totalTextSize == 0) {
            return "";
        }
        StringBuilder numbValue = new StringBuilder("");

        int intNumStartIndex = lastOperandIndex;
        if (intNumStartIndex >= totalTextSize) {
            // we have pressed btn '='
            return "=";
        }

        while (intNumStartIndex != totalTextSize &&
                !Character.isDigit(charSequence.charAt(intNumStartIndex))) {
            intNumStartIndex++;
        }

        for (int i = intNumStartIndex; i < totalTextSize; i++) {
            numbValue.append(charSequence.charAt(i));
        }
        return numbValue.toString();
    }

    // Метод для обработки инженерных операций
    // Возвращает true, если операция успешно обработана
    // can be squareRoot, sin, cos, log
    // returns false if it's not handled or not engineer commands
    private boolean handleEngineerOperation(String command) {
        if (!engineerCommands.contains(command)) {
            return false;
        }

        String lastNumStringResult = getLastNumbString();
        if (lastNumStringResult.isEmpty()) {
            lastNumStringResult = "0";
        }
        if (lastNumStringResult.equals("")) {

            // Специальный случай, когда введено значение (PI, E)
            // here is specific case when we enter value (PI, E)
            if (command.equals("e") || command.equals(PI_Code)) {
                swapLastNumber(Float.toString(performEngineerOperation(command, 0F)));
                return true;
            }

            return false;
        }

        if (lastNumStringResult.charAt(lastNumStringResult.length() - 1) == '.') {
            lastNumStringResult += "0";
        }

        float number;
        try {
            number = Float.parseFloat(lastNumStringResult);
        } catch (Exception e) {
            return false;
        }

        number = performEngineerOperation(command, number);
        swapLastNumber(Float.toString(number));
        return true;
    }

    // Метод для вычисления выражения
    private Float calculateExpression() {
        if (operationsInARow.size() == 0 || !operationAfterNumberWas) {
            if (inputView.getText().length() > 0) {
                lastResult = Float.parseFloat(inputView.getText().toString());
            }
            return performOperation(lastInputOperation, lastResult, lastInputValue);
        }

        Float result = numbersByItsOrder.size() == 1 ? numbersByItsOrder.get(0) : 0F;
        ArrayList<Float> tempNumbers = new ArrayList<>(numbersByItsOrder);
        ArrayList<Operations> tempOperations = new ArrayList<>(operationsInARow);
        while (tempNumbers.size() > 1) {
            int opIndexToPerformFirst = getPriorityOperationIndex(tempOperations);
            if (opIndexToPerformFirst < 0) {
                return result;
            }

            Float leftValue = tempNumbers.get(opIndexToPerformFirst);
            Float rightValue = tempNumbers.get(opIndexToPerformFirst + 1);
            result = performOperation(tempOperations.get(opIndexToPerformFirst), leftValue, rightValue);

            tempNumbers.remove(opIndexToPerformFirst);
            tempNumbers.remove(opIndexToPerformFirst);
            tempNumbers.add(opIndexToPerformFirst, result);
            tempOperations.remove(opIndexToPerformFirst);
        }

        return result;
    }

    // Метод для определения индекса последней операции в выражении
    private int getAvailableLastOperandIndex() {
        int index = 0;
        int inputTextSize = inputView.length();
        CharSequence inputText = inputView.getText();
        for (int i = 1; i < inputTextSize; i++) {
            Character symbol = inputText.charAt(i);
            if (!Character.isDigit(symbol) && symbol != '.'
                    && deleteDefaultOperationSpecialCases.contains(inputText.charAt(i))) {
                index = i;
            }
        }
        return index;
    }

    // Метод для определения индекса операции с наивысшим приоритетом
    private int getPriorityOperationIndex(ArrayList<Operations> operations) {
        int minIndex = -1;
        int minOpValue = 0;

        int index = 0;

        // Operations enum уже сгруппированы с учетом порядка приоритета
        // Operations enum is already grouped with ordinal values = priority
        for (Operations op : operations) {
            if (op.ordinal() > minOpValue) {
                minIndex = index;
                minOpValue = op.ordinal();
            }
            index++;
        }
        return minIndex;
    }

    // Метод для парсинга символа операции
    private Operations parseOperation(String input) {
        switch (input) {
            case "+":
                return Operations.Plus;
            case "-":
                return Operations.Minus;
            case "*":
                return Operations.Multiply;
            case "/":
                return Operations.Divide;
        }
        return Operations.None;
    }

    // Метод для инициализации списка символов операций, которые можно удалять
    private void initDeleteCases() {
        deleteDefaultOperationSpecialCases.add('-');
        deleteDefaultOperationSpecialCases.add('+');
        deleteDefaultOperationSpecialCases.add('/');
        deleteDefaultOperationSpecialCases.add('*');
    }

    // Метод для инициализации списка инженерных команд
    private void initCommands() {
        engineerCommands.add("sin");
        engineerCommands.add("cos");
        engineerCommands.add("ln");
        engineerCommands.add(Sqrt_Code);
        engineerCommands.add("e");
        engineerCommands.add(PI_Code);
    }

    // Метод для выполнения операции над двумя числами
    private float performOperation(Operations op, Float leftVal, Float rightVal) {
        switch (op) {
            case Plus:
                return leftVal + rightVal;
            case Minus:
                return leftVal - rightVal;
            case Divide:
                return leftVal / rightVal;
            case Multiply:
                return leftVal * rightVal;
        }
        return 0f;
    }

    // Метод для выполнения инженерных операций
    private float performEngineerOperation(String op, Float value) {
        switch (op) {
            case "sin":
                return (float) Math.sin(value);
            case "cos":
                return (float) Math.cos(value);
            case "ln":
                return (float) Math.log(value);
            case Sqrt_Code:
                return (float) Math.sqrt(value);
            case PI_Code:
                return PI;
            case "e":
                return E;
        }
        return value;
    }
}
