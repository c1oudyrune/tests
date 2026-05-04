import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.ZoneId;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


abstract class AbstractProduct {
    private String id;
    private String name;
    private double price;
    private int quantity;

    public AbstractProduct(String id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }


    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return String.format("ID: %s, Название: %s, Цена: %.2f, Кол-во: %d", id, name, price, quantity);
    }
}


class FoodProduct extends AbstractProduct {
    private LocalDate expiryDate;

    public FoodProduct(String id, String name, double price, int quantity, LocalDate expiryDate) {
        super(id, name, price, quantity);
        this.expiryDate = expiryDate;
    }

    public LocalDate getExpiryDate() { return expiryDate; }

    @Override
    public String toString() {
        return super.toString() + String.format(", Срок годности: %s", expiryDate);
    }
}


class ElectronicsProduct extends AbstractProduct {
    private int warrantyDays;

    public ElectronicsProduct(String id, String name, double price, int quantity, int warrantyDays) {
        super(id, name, price, quantity);
        this.warrantyDays = warrantyDays;
    }

    public int getWarrantyDays() { return warrantyDays; }

    @Override
    public String toString() {
        return super.toString() + String.format(", Гарантия: %d дн.", warrantyDays);
    }
}


class Warehouse {
    private Map<String, AbstractProduct> products = new LinkedHashMap<>();
    private static final String DATA_FILE = "warehouse_data.csv";
    private static final String SEPARATOR = ";";


    private static Warehouse instance;
    private Warehouse() {
        loadData();
        startBackgroundNotifications();
    }
    public static synchronized Warehouse getInstance() {
        if (instance == null) {
            instance = new Warehouse();
        }
        return instance;
    }


    public void addProduct(AbstractProduct product) {
        products.put(product.getId(), product);
        saveData();
    }


    public boolean removeProduct(String id) {
        AbstractProduct removed = products.remove(id);
        if (removed != null) {
            saveData();
            return true;
        }
        return false;
    }


    public Collection<AbstractProduct> getAllProducts() {
        return Collections.unmodifiableCollection(products.values());
    }


    public List<AbstractProduct> findProductsByName(String name) {
        return products.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }


    public AbstractProduct getProductById(String id) {
        return products.get(id);
    }


    public double getTotalValue() {
        return products.values().stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
    }


    public List<AbstractProduct> getLowStockProducts(int threshold) {
        return products.values().stream()
                .filter(p -> p.getQuantity() < threshold)
                .collect(Collectors.toList());
    }


    private void saveData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (AbstractProduct product : products.values()) {
                writer.write(productToCsvString(product));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
        }
    }


    private void loadData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                products.put(csvStringToProduct(line).getId(), csvStringToProduct(line));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Файл данных не найден. Создается новый склад.");
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке данных: " + e.getMessage());
        }
    }


    private String productToCsvString(AbstractProduct product) {
        StringBuilder sb = new StringBuilder();
        sb.append(product.getClass().getSimpleName()).append(SEPARATOR);
        sb.append(product.getId()).append(SEPARATOR);
        sb.append(product.getName()).append(SEPARATOR);
        sb.append(product.getPrice()).append(SEPARATOR);
        sb.append(product.getQuantity());
        if (product instanceof FoodProduct) {

            sb.append(SEPARATOR).append(((FoodProduct) product).getExpiryDate().toString());
        } else if (product instanceof ElectronicsProduct) {
            sb.append(SEPARATOR).append(((ElectronicsProduct) product).getWarrantyDays());
        }
        return sb.toString();
    }


    private AbstractProduct csvStringToProduct(String line) {
        String[] parts = line.split(SEPARATOR);
        String type = parts[0];
        String id = parts[1];
        String name = parts[2];
        double price = Double.parseDouble(parts[3]);
        int quantity = Integer.parseInt(parts[4]);

        if ("FoodProduct".equals(type)) {
            LocalDate expiryDate = LocalDate.parse(parts[5]);
            return new FoodProduct(id, name, price, quantity, expiryDate);
        } else if ("ElectronicsProduct".equals(type)) {
            int warrantyDays = Integer.parseInt(parts[5]);
            return new ElectronicsProduct(id, name, price, quantity, warrantyDays);
        }
        throw new IllegalArgumentException("Неизвестный тип продукта: " + type);
    }


    private void startBackgroundNotifications() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("\n[УВЕДОМЛЕНИЕ] Проверка товаров...");
            LocalDate now = LocalDate.now();
            List<FoodProduct> almostExpired = products.values().stream()
                    .filter(p -> p instanceof FoodProduct)
                    .map(p -> (FoodProduct) p)
                    .filter(fp -> fp.getExpiryDate().isBefore(now.plusDays(7)) && fp.getQuantity() > 0)
                    .collect(Collectors.toList());

            if (!almostExpired.isEmpty()) {
                System.out.println("Внимание! Скоро истекает срок годности у следующих товаров:");
                almostExpired.forEach(fp -> System.out.println("- " + fp.getName() + " (ID: " + fp.getId() + "), срок до: " + fp.getExpiryDate()));
            } else {
                System.out.println("Товаров с приближающимся сроком годности не обнаружено.");
            }
            System.out.println("----------------------------------------");
        }, 30, 30, TimeUnit.SECONDS);
    }


    public static String readStringInputNonEmpty(String prompt) {
        Scanner scanner = new Scanner(System.in);
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Поле не может быть пустым.");
            }
        } while (input.isEmpty());
        return input;
    }

    public static String readIdInput(String prompt) {
        String id = readStringInputNonEmpty(prompt);

        return id;
    }

    public static double readDoubleInput(String prompt) {
        Scanner scanner = new Scanner(System.in);
        double value = -1.0;
        while (value < 0) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine();
                value = Double.parseDouble(input);
                if (value < 0) {
                    System.out.println("Цена не может быть отрицательной.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка ввода: Пожалуйста, введите число (например, 10.50).");
            }
        }
        return value;
    }

    public static int readIntInput(String prompt) {
        Scanner scanner = new Scanner(System.in);
        int value = -1;
        while (value < 0) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine();
                value = Integer.parseInt(input);
                if (value < 0) {
                    System.out.println("Количество не может быть отрицательным.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка ввода: Пожалуйста, введите целое число.");
            }
        }
        return value;
    }

    public static LocalDate readDateInput(String prompt) {
        Scanner scanner = new Scanner(System.in);
        LocalDate date = null;
        while (date == null) {
            System.out.print(prompt + " (в формате YYYY-MM-DD): ");
            String dateStr = scanner.nextLine();
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                System.out.println("Ошибка ввода даты. Убедитесь, что формат YYYY-MM-DD и дата корректна.");
            }
        }
        return date;
    }
}


class WarehouseMaster {

    public static void main(String[] args) {
        Warehouse warehouse = Warehouse.getInstance();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Добро пожаловать в Систему складского учета 'Warehouse Master'!");

        int choice;
        do {
            printMenu();
            choice = Warehouse.readIntInput("Выберите действие: ");

            switch (choice) {
                case 1:
                    addProductInteractive(warehouse, scanner);
                    break;
                case 2:
                    removeProductInteractive(warehouse);
                    break;
                case 3:
                    displayAllProducts(warehouse);
                    break;
                case 4:
                    searchProductByName(warehouse, scanner);
                    break;
                case 5:
                    displayTotalValue(warehouse);
                    break;
                case 6:
                    displayLowStockProducts(warehouse, scanner);
                    break;
                case 0:
                    System.out.println("Выход из программы. Данные сохранены.");
                    break;
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
            }
        } while (choice != 0);


        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n--- Меню ---");
        System.out.println("1. Добавить товар (с разными типами ID)");
        System.out.println("2. Удалить товар по ID");
        System.out.println("3. Показать все товары");
        System.out.println("4. Поиск товара по названию");
        System.out.println("5. Показать общую стоимость склада");
        System.out.println("6. Показать товары с низким остатком");
        System.out.println("0. Выход");
        System.out.println("------------");
    }

    private static void addProductInteractive(Warehouse warehouse, Scanner scanner) {
        System.out.println("\n--- Добавление товара ---");
        int typeChoice = Warehouse.readIntInput("Тип товара (1 - Продукты питания, 2 - Электроника): ");
        String id = Warehouse.readIdInput("Введите уникальный ID товара: "); // Теперь String ID


        if (warehouse.getProductById(id) != null) {
            System.out.println("Ошибка: Товар с таким ID уже существует.");
            return;
        }

        String name = Warehouse.readStringInputNonEmpty("Введите название товара: ");
        double price = Warehouse.readDoubleInput("Введите цену товара: ");
        int quantity = Warehouse.readIntInput("Введите количество товара: ");

        AbstractProduct product = null;
        if (typeChoice == 1) {
            LocalDate expiryDate = Warehouse.readDateInput("Введите срок годности");
            product = new FoodProduct(id, name, price, quantity, expiryDate);
        } else if (typeChoice == 2) {
            int warrantyDays = Warehouse.readIntInput("Введите гарантийный срок (в днях): ");
            product = new ElectronicsProduct(id, name, price, quantity, warrantyDays);
        } else {
            System.out.println("Неверный выбор типа товара.");
            return;
        }

        if (product != null) {
            warehouse.addProduct(product);
            System.out.println("Товар успешно добавлен.");
        }
    }

    private static void removeProductInteractive(Warehouse warehouse) {
        System.out.println("\n--- Удаление товара ---");
        String id = Warehouse.readIdInput("Введите ID товара для удаления: ");
        if (warehouse.removeProduct(id)) {
            System.out.println("Товар с ID '" + id + "' успешно удален.");
        } else {
            System.out.println("Товар с ID '" + id + "' не найден.");
        }
    }

    private static void displayAllProducts(Warehouse warehouse) {
        System.out.println("\n--- Все товары на складе ---");
        Collection<AbstractProduct> products = warehouse.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("Склад пуст.");
        } else {
            products.forEach(System.out::println);
        }
    }

    private static void searchProductByName(Warehouse warehouse, Scanner scanner) {
        System.out.println("\n--- Поиск товара ---");
        String name = Warehouse.readStringInputNonEmpty("Введите часть названия товара для поиска: ");
        List<AbstractProduct> foundProducts = warehouse.findProductsByName(name);
        if (foundProducts.isEmpty()) {
            System.out.println("Товары с таким названием не найдены.");
        } else {
            System.out.println("Найденные товары:");
            foundProducts.forEach(System.out::println);
        }
    }

    private static void displayTotalValue(Warehouse warehouse) {
        System.out.println("\n--- Общая стоимость склада ---");
        System.out.printf("Общая стоимость всех товаров: %.2f\n", warehouse.getTotalValue());
    }

    private static void displayLowStockProducts(Warehouse warehouse, Scanner scanner) {
        System.out.println("\n--- Товары с низким остатком ---");
        int threshold = Warehouse.readIntInput("Введите минимальный порог остатка: ");
        List<AbstractProduct> lowStockProducts = warehouse.getLowStockProducts(threshold);
        if (lowStockProducts.isEmpty()) {
            System.out.println("Товаров с остатком ниже " + threshold + " не обнаружено.");
        } else {
            System.out.println("Товары с остатком < " + threshold + ":");
            lowStockProducts.forEach(System.out::println);
        }
    }
}
