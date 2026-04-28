import operator

def add(x, y):

    return x + y

def subtract(x, y):

    return x - y

def multiply(x, y):

    return x * y

def divide(x, y):

    if y == 0:
        raise ValueError("Деление на ноль невозможно!")
    return x / y

def get_operation(operator_char):

    operations = {
        '+': add,
        '-': subtract,
        '*': multiply,
        '/': divide
    }
    if operator_char not in operations:
        raise ValueError(f"Недопустимая операция: {operator_char}. Допустимые операции: +, -, *, /")
    return operations[operator_char]

def calculator():

    print("Простой калькулятор Python")
    print("Доступные операции: +, -, *, /")
    print("Введите 'quit' для выхода.")

    while True:
        try:

            num1_str = input("Введите первое число: ")
            if num1_str.lower() == 'quit':
                break
            num1 = float(num1_str)


            op_str = input("Введите операцию (+, -, *, /): ")
            if op_str.lower() == 'quit':
                break
            operation = get_operation(op_str)


            num2_str = input("Введите второе число: ")
            if num2_str.lower() == 'quit':
                break
            num2 = float(num2_str)


            result = operation(num1, num2)
            print(f"Результат: {num1} {op_str} {num2} = {result}")

        except ValueError as ve:
            print(f"Ошибка ввода: {ve}")
        except Exception as e:
            print(f"Произошла ошибка: {e}")

        print("-" * 20)

if __name__ == "__main__":
    calculator()
