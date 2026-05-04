import allure
import pytest
from calculator import add, subtract, multiply, divide, get_operation
from allure_commons.types import LabelType


@allure.feature('Калькулятор')
class TestCalculator:

    
    @allure.story('Сложение')
    @allure.severity(severity_level=allure.severity_level.NORMAL)
    @pytest.mark.parametrize("num1, num2, expected", [
        (5, 3, 8),
        (-5, -3, -8),
        (0, 0, 0),
        (10, -4, 6),
        (0, 7, 7),
        (7, 0, 7),
        (1.5, 2.5, 4.0),
    ])
    def test_add(self, num1, num2, expected):
        
        with allure.step(f"Выполняем сложение: {num1} + {num2}"):
            result = add(num1, num2)
            allure.attach(f"Ожидаемый результат: {expected}, Фактический результат: {result}", name="Результат сложения", attachment_type=allure.attachment_type.TEXT)
            assert result == expected
        

    @allure.story('Вычитание')
    @allure.severity(severity_level=allure.severity_level.NORMAL)
    @pytest.mark.parametrize("num1, num2, expected", [
        (10, 5, 5),
        (5, 10, -5),
        (-10, -5, -5),
        (0, 0, 0),
        (7, 0, 7),
        (0, 7, -7),
        (3.14, 1.14, 2.0),
    ])
    def test_subtract(self, num1, num2, expected):
        with allure.step(f"Выполняем вычитание: {num1} - {num2}"):
            result = subtract(num1, num2)
            allure.attach(f"Ожидаемый результат: {expected}, Фактический результат: {result}", name="Результат вычитания", attachment_type=allure.attachment_type.TEXT)
            assert result == expected

    @allure.story('Умножение')
    @allure.severity(severity_level=allure.severity_level.NORMAL)
    @pytest.mark.parametrize("num1, num2, expected", [
        (5, 3, 15),
        (-5, 3, -15),
        (-5, -3, 15),
        (10, 0, 0),
        (0, 10, 0),
        (0, 0, 0),
        (1, 5, 5),
        (5, 1, 5),
        (2.5, 2.0, 5.0),
    ])
    def test_multiply(self, num1, num2, expected):
        with allure.step(f"Выполняем умножение: {num1} * {num2}"):
            result = multiply(num1, num2)
            allure.attach(f"Ожидаемый результат: {expected}, Фактический результат: {result}", name="Результат умножения", attachment_type=allure.attachment_type.TEXT)
            assert result == expected

    @allure.story('Деление')
    @allure.severity(severity_level=allure.severity_level.NORMAL)
    @pytest.mark.parametrize("num1, num2, expected", [
        (10, 2, 5.0),
        (10, -2, -5.0),
        (-10, -2, 5.0),
        (7, 2, 3.5),
        (0, 5, 0.0),
        (1, 1, 1.0),
        (5, 1, 5.0),
        (100, 0.5, 200.0),
    ])
    def test_divide(self, num1, num2, expected):
        with allure.step(f"Выполняем деление: {num1} / {num2}"):
            result = divide(num1, num2)
            allure.attach(f"Ожидаемый результат: {expected}, Фактический результат: {result}", name="Результат деления", attachment_type=allure.attachment_type.TEXT)
            assert result == expected

    @allure.story('Деление на ноль')
    @allure.severity(severity_level=allure.severity_level.CRITICAL) # Отмечаем как критический случай
    def test_divide_by_zero(self):
        with allure.step("Попытка деления на ноль"):
            with pytest.raises(ValueError, match="Деление на ноль невозможно!"):
                divide(10, 0)
            allure.attach("Деление на ноль успешно перехвачено как ValueError.", name="Проверка деления на ноль", attachment_type=allure.attachment_type.TEXT)


    @allure.story('Получение функции операции')
    @allure.severity(severity_level=allure.severity_level.NORMAL)
    @pytest.mark.parametrize("op_char, expected_func", [
        ('+', add),
        ('-', subtract),
        ('*', multiply),
        ('/', divide),
    ])
    def test_get_operation_valid(self, op_char, expected_func):
        with allure.step(f"Запрос функции для символа операции: '{op_char}'"):
            func = get_operation(op_char)
            allure.attach(f"Ожидаемая функция: {expected_func.__name__}, Полученная функция: {func.__name__}", name="Результат get_operation", attachment_type=allure.attachment_type.TEXT)
            assert func == expected_func

    @allure.story('Получение функции операции (недопустимый символ)')
    @allure.severity(severity_level=allure.severity_level.NORMAL)
    @pytest.mark.parametrize("invalid_op_char", [
        ('%'),
        ('^'),
        ('add'),
        (''),
        ('?'),
    ])
    def test_get_operation_invalid(self, invalid_op_char):
        with allure.step(f"Попытка получения функции для недопустимого символа: '{invalid_op_char}'"):
            with pytest.raises(ValueError, match="Недопустимая операция:"):
                get_operation(invalid_op_char)
            allure.attach(f"Недопустимый символ '{invalid_op_char}' успешно вызвал ValueError.", name="Проверка недопустимого символа", attachment_type=allure.attachment_type.TEXT)


    @allure.story('Комбинированные операции')
    def test_functional_add_then_multiply(self):
        num1 = 5
        num2 = 3
        num3 = 2

        with allure.step(f"Выполняем сложение: {num1} + {num2}"):
            intermediate_result = add(num1, num2) # Ожидаем 8
            allure.attach(f"Промежуточный результат сложения: {intermediate_result}", name="Промежуточный результат", attachment_type=allure.attachment_type.TEXT)
            assert intermediate_result == 8

        with allure.step(f"Выполняем умножение: {intermediate_result} * {num3}"):
            final_result = multiply(intermediate_result, num3) # Ожидаем 8 * 2 = 16
            allure.attach(f"Финальный результат умножения: {final_result}", name="Финальный результат", attachment_type=allure.attachment_type.TEXT)
            assert final_result == 16

    @allure.story('Комбинированные операции (с ошибкой)')
    def test_functional_division_with_subsequent_error(self):
        num1 = 10
        num2 = 0
        num3 = 5

        with allure.step(f"Попытка деления: {num1} / {num2}"):
            with pytest.raises(ValueError, match="Деление на ноль невозможно!"):
                divide(num1, num2)
            allure.attach("Ошибка деления на ноль успешно перехвачена.", name="Проверка ошибки при делении", attachment_type=allure.attachment_type.TEXT)

    @allure.story('Граничные случаи (малые отрицательные числа)')
    def test_add_very_small_negative(self):
        with allure.step("Сложение двух очень малых отрицательных чисел"):
            result = add(-1.0e-300, -1.0e-300)
            allure.attach(f"Результат: {result}", name="Результат сложения малых отрицательных чисел", attachment_type=allure.attachment_type.TEXT)
            assert result == -2.0e-300

    @allure.story('Граничные случаи (большое из малого)')
    def test_subtract_large_from_small(self):
        with allure.step("Вычитание большого числа из малого"):
            num1 = 1000.0
            num2 = 1.0e-9
            result = subtract(num1, num2)
            allure.attach(f"Вычитание: {num1} - {num2} = {result}", name="Результат вышедшего вычитания", attachment_type=allure.attachment_type.TEXT)
            assert result == pytest.approx(1000.0)

    @allure.story('Граничные случаи (умножение на малое)')
    def test_multiply_by_very_small_number(self):
        with allure.step("Умножение на очень малое число"):
            num1 = 100
            num2 = 1.0e-10
            result = multiply(num1, num2)
            allure.attach(f"Умножение: {num1} * {num2} = {result}", name="Результат умножения на малое число", attachment_type=allure.attachment_type.TEXT)
            assert result == pytest.approx(1.0e-8)

    @allure.story('Тип результата деления')
    def test_integer_division_result_type(self):
        with allure.step("Проверка типа результата целочисленного деления"):
            result = divide(5, 2)
            allure.attach(f"Результат: {result}, Тип: {type(result)}", name="Тип результата деления", attachment_type=allure.attachment_type.TEXT)
            assert isinstance(result, float)
            assert result == 2.5

    @allure.story('Граничные случаи (деление на малое положительное)')
    def test_divide_by_small_positive_number(self):
        with allure.step("Деление на очень малое положительное число"):
            num1 = 10
            num2 = 1.0e-12
            result = divide(num1, num2)
            allure.attach(f"Деление: {num1} / {num2} = {result}", name="Результат деления на малое положительное число", attachment_type=allure.attachment_type.TEXT)
            assert result == 1.0e13

    @allure.story('Получение функции операции (пустая строка)')
    def test_get_operation_empty_string(self):
        with allure.step("Попытка получения функции для пустой строки"):
            with pytest.raises(ValueError, match="Недопустимая операция:"):
                get_operation("")
            allure.attach("Пустая строка успешно вызвала ValueError.", name="Проверка пустой строки", attachment_type=allure.attachment_type.TEXT)
