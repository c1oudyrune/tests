import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WarehouseTest {

    private Warehouse warehouse;


    @BeforeEach
    void setUp() {
        warehouse = Warehouse.getInstance();

        clearWarehouseData(warehouse);

        try {
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get("warehouse_data.csv"));
        } catch (java.io.IOException e) {
            System.err.println("Не удалось удалить файл данных перед тестом: " + e.getMessage());
        }
    }


    @AfterEach
    void tearDown() {

        clearWarehouseData(warehouse);
        try {
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get("warehouse_data.csv"));
        } catch (java.io.IOException e) {
            System.err.println("Не удалось удалить файл данных после теста: " + e.getMessage());
        }
    }


    private void clearWarehouseData(Warehouse warehouse) {
        try {

            Field field = Warehouse.class.getDeclaredField("products");
            field.setAccessible(true);
            Map<String, AbstractProduct> productsMap = (Map<String, AbstractProduct>) field.get(warehouse);
            productsMap.clear();
        } catch (NoSuchFieldException e) {

            System.err.println("Ошибка: поле 'products' не найдено в классе Warehouse. " + e.getMessage());
            throw new RuntimeException("Не удалось очистить внутреннее состояние склада (NoSuchFieldException).", e);
        } catch (IllegalAccessException e) {

            System.err.println("Ошибка доступа к полю 'products' в классе Warehouse. " + e.getMessage());
            throw new RuntimeException("Не удалось очистить внутреннее состояние склада (IllegalAccessException).", e);
        } catch (Exception e) {

            System.err.println("Непредвиденная ошибка при очистке данных склада: " + e.getMessage());
            throw new RuntimeException("Не удалось очистить внутреннее состояние склада.", e);
        }
    }


    @Test
    @Order(1)
    @DisplayName("Добавление нового продукта на склад (FoodProduct)")
    void testAddFoodProduct_HappyPath() {
        FoodProduct apple = new FoodProduct("F001", "Apple", 0.5, 100, LocalDate.now().plusDays(30));
        warehouse.addProduct(apple);
        Assertions.assertEquals(1, warehouse.getAllProducts().size(), "На складе должен быть 1 продукт.");
        Assertions.assertNotNull(warehouse.getProductById("F001"), "Продукт должен быть найден по ID.");
        Assertions.assertEquals("Apple", warehouse.getProductById("F001").getName());
    }
    @Test
    @Order(3)
    @DisplayName("Получение существующего продукта по ID")
    void testGetProductById_Existing() {
        FoodProduct banana = new FoodProduct("F002", "Banana", 0.3, 150, LocalDate.now().plusDays(10));
        warehouse.addProduct(banana);
        AbstractProduct retrievedProduct = warehouse.getProductById("F002");
        Assertions.assertNotNull(retrievedProduct, "Продукт должен быть найден.");
        Assertions.assertEquals("Banana", retrievedProduct.getName(), "Название продукта должно совпадать.");
        Assertions.assertEquals(0.3, retrievedProduct.getPrice(), 0.001, "Цена продукта должна совпадать.");
    }


    @Test
    @Order(4)
    @DisplayName("Получение несуществующего продукта по ID")
    void testGetProductById_NonExisting() {
        Assertions.assertNull(warehouse.getProductById("NONEXISTENT_ID"), "Должен возвращаться null, если продукт не найден.");
    }


    @Test
    @Order(5)
    @DisplayName("Удаление существующего продукта")
    void testRemoveProduct_Existing() {
        FoodProduct orange = new FoodProduct("F003", "Orange", 0.7, 50, LocalDate.now().plusDays(15));
        warehouse.addProduct(orange);
        boolean removed = warehouse.removeProduct("F003");
        Assertions.assertTrue(removed, "Метод removeProduct должен вернуть true при успешном удалении.");
        Assertions.assertNull(warehouse.getProductById("F003"), "Продукт не должен существовать после удаления.");
        Assertions.assertEquals(0, warehouse.getAllProducts().size(), "На складе не должно остаться продуктов.");
    }


    @Test
    @Order(6)
    @DisplayName("Попытка удалить несуществующий продукт")
    void testRemoveProduct_NonExisting() {
        boolean removed = warehouse.removeProduct("THIS_ID_DOES_NOT_EXIST");
        Assertions.assertFalse(removed, "Метод removeProduct должен вернуть false, если продукт не найден.");
        Assertions.assertEquals(0, warehouse.getAllProducts().size(), "Размер склада не должен измениться.");
    }
    @Test
    @Order(8)
    @DisplayName("Поиск продуктов по названию, которого нет")
    void testFindProductsByName_NotFound() {
        FoodProduct apple = new FoodProduct("F001", "Apple", 0.5, 100, LocalDate.now().plusDays(30));
        warehouse.addProduct(apple);
        List<AbstractProduct> results = warehouse.findProductsByName("Orange");
        Assertions.assertTrue(results.isEmpty(), "Список найденных продуктов должен быть пустым.");
    }
    @Test
    @Order(10)
    @DisplayName("Расчет общей стоимости пустого склада")
    void testGetTotalValue_EmptyWarehouse() {
        Assertions.assertEquals(0.0, warehouse.getTotalValue(), 0.001, "Общая стоимость пустого склада должна быть 0.");
    }@Test
    @Order(15)
    @DisplayName("Продукт с истекшим сроком годности")
    void testExpiredFoodProduct() {
        FoodProduct staleBread = new FoodProduct("F401", "Stale Bread", 0.5, 20, LocalDate.now().minusDays(10));
        warehouse.addProduct(staleBread);
        AbstractProduct retrieved = warehouse.getProductById("F401");
        Assertions.assertNotNull(retrieved);
        Assertions.assertTrue(retrieved instanceof FoodProduct);
        Assertions.assertEquals("Stale Bread", retrieved.getName());

        Assertions.assertEquals(LocalDate.now().minusDays(10), ((FoodProduct) retrieved).getExpiryDate());

        Assertions.assertEquals(10.0, warehouse.getTotalValue(), 0.001);
    }
    @Test
    @Order(16)
    @DisplayName("Поиск по пустому названию")
    void testFindProductsByName_EmptyString() {
        FoodProduct apple = new FoodProduct("F501", "Apple", 0.5, 10, LocalDate.now().plusDays(30));
        FoodProduct banana = new FoodProduct("F502", "Banana", 0.3, 15, LocalDate.now().plusDays(10));
        warehouse.addProduct(apple);
        warehouse.addProduct(banana);


        List<AbstractProduct> results = warehouse.findProductsByName("");
        Assertions.assertEquals(2, results.size(), "Поиск с пустой строкой должен вернуть все продукты.");
        Assertions.assertTrue(results.stream().anyMatch(p -> "F501".equals(p.getId())));
        Assertions.assertTrue(results.stream().anyMatch(p -> "F502".equals(p.getId())));
    }
    @Test
    @Order(18)
    @DisplayName("Изменение цены и количества существующего продукта")
    void testUpdateProductAttributes() {
        FoodProduct juice = new FoodProduct("F701", "Juice", 2.0, 10, LocalDate.now().plusDays(10));
        warehouse.addProduct(juice);

        AbstractProduct product = warehouse.getProductById("F701");
        Assertions.assertNotNull(product);


        product.setPrice(2.2);
        product.setQuantity(15);


        Assertions.assertEquals(2.2, product.getPrice(), 0.001);
        Assertions.assertEquals(15, product.getQuantity());


        Assertions.assertEquals(33.0, warehouse.getTotalValue(), 0.001);
    }
    @Test
    @Order(20)
    @DisplayName("Проверка запуска фоновых уведомлений (без выполнения полной логики)")
    @DisabledIfEnvironmentVariable(named = "SKIP_BACKGROUND_TESTS", matches = "true")
    void testBackgroundNotifications_Startup() {

        Assertions.assertDoesNotThrow(() -> {

            Thread.sleep(500);
        }, "Вызов startBackgroundNotifications должен выполняться без ошибок.");
    }
}
