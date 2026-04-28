
import pytest
from calculator import add, subtract, multiply, divide, get_operation

# тесты для отдельных функций

@pytest.mark.parametrize("num1, num2, expected", [
    (5, 3, 8),
    (-5, -3, -8),
    (0, 0, 0),
    (10, -4, 6),
    (0, 7, 7),
    (7, 0, 7),
    (1.5, 2.5, 4.0),
])
def test_add(num1, num2, expected):

    assert add(num1, num2) == expected


    assert add(10**100, 1) == 10**100 + 1


@pytest.mark.parametrize("num1, num2, expected", [
    (10, 5, 5),
    (5, 10, -5),
    (-10, -5, -5),
    (0, 0, 0),
    (7, 0, 7),
    (0, 7, -7),
    (3.14, 1.14, 2.0),
])
def test_subtract(num1, num2, expected):

    assert subtract(num1, num2) == expected


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
def test_multiply(num1, num2, expected):
    
    assert multiply(num1, num2) == expected


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
def test_divide(num1, num2, expected):
    
    assert divide(num1, num2) == expected


def test_divide_by_zero():
    
    with pytest.raises(ValueError, match="Деление на ноль невозможно!"):
        divide(10, 0)


@pytest.mark.parametrize("op_char, expected_func", [
    ('+', add),
    ('-', subtract),
    ('*', multiply),
    ('/', divide),
])
def test_get_operation_valid(op_char, expected_func):

    assert get_operation(op_char) == expected_func


@pytest.mark.parametrize("invalid_op_char", [
    ('%'),
    ('^'),
    ('add'),
    (''),
    ('?'),
])
def test_get_operation_invalid(invalid_op_char):

    with pytest.raises(ValueError, match="Недопустимая операция:"):
        get_operation(invalid_op_char)



def test_functional_add_then_multiply():

    num1 = 5
    num2 = 3
    num3 = 2


    intermediate_result = add(num1, num2) # Ожидаем 8
    assert intermediate_result == 8


    final_result = multiply(intermediate_result, num3) # Ожидаем 8 * 2 = 16
    assert final_result == 16


def test_functional_division_with_subsequent_error():

    num1 = 10
    num2 = 0
    num3 = 5


    with pytest.raises(ValueError, match="Деление на ноль невозможно!"):
        divide(num1, num2)



def test_add_very_small_negative():
    """Тест сложения двух очень маленьких отрицательных чисел."""
    assert add(-1.0e-300, -1.0e-300) == -2.0e-300


def test_subtract_large_from_small():

    assert subtract(1000.0, 1.0e-9) == pytest.approx(1000.0) 


def test_multiply_by_very_small_number():

    assert multiply(100, 1.0e-10) == pytest.approx(1.0e-8)


def test_integer_division_result_type():

    assert isinstance(divide(5, 2), float)
    assert divide(5, 2) == 2.5


def test_divide_by_small_positive_number():

    assert divide(10, 1.0e-12) == 1.0e13


def test_get_operation_empty_string():

    with pytest.raises(ValueError, match="Недопустимая операция:"):
        get_operation("")
