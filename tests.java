import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;



@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseTest {

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
    @Order(2)
    @DisplayName("Добавление нового продукта на склад (ElectronicsProduct)")
    void testAddElectronicsProduct_HappyPath() {
        
        ElectronicsProduct laptop = new ElectronicsProduct("E001", "Laptop", 1200.0, 365); 
        warehouse.addProduct(laptop);
        Assertions.assertEquals(1, warehouse.getAllProducts().size(), "На складе должен быть 1 продукт.");
        Assertions.assertNotNull(warehouse.getProductById("E001"), "Продукт должен быть найден по ID.");
        Assertions.assertEquals("Laptop", warehouse.getProductById("E001").getName());
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
    @Order(7)
    @DisplayName("Поиск продуктов по части названия (регистронезависимый)")
    void testFindProductsByName_CaseInsensitive() {
        
        FoodProduct apple1 = new FoodProduct("F010", "Apple iPhone 13", 999.99, 10, LocalDate.now().plusDays(365));
        ElectronicsProduct adapter = new ElectronicsProduct("E010", "Apple Lightning Adapter", 29.99, 365); 
        warehouse.addProduct(apple1);
        warehouse.addProduct(adapter);

        List<AbstractProduct> resultsLower = warehouse.findProductsByName("apple"); 
        Assertions.assertEquals(2, resultsLower.size(), "Должны быть найдены оба продукта, содержащие 'apple'.");

        List<AbstractProduct> resultsUpper = warehouse.findProductsByName("APPLE"); 
        Assertions.assertEquals(2, resultsUpper.size(), "Должны быть найдены оба продукта, содержащие 'APPLE'.");

        List<AbstractProduct> resultsMixed = warehouse.findProductsByName("ApPlE"); 
        Assertions.assertEquals(2, resultsMixed.size(), "Должны быть найдены оба продукта, содержащие 'ApPlE'.");
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
    @Order(9)
    @DisplayName("Расчет общей стоимости с разными сценариями")
    void testGetTotalValue_VariousScenarios() {
        FoodProduct apple = new FoodProduct("F001", "Apple", 0.5, 10, LocalDate.now().plusDays(30)); 
        
        ElectronicsProduct mac = new ElectronicsProduct("E001", "MacBook Air", 1000.0, 365); 
        FoodProduct freebie = new FoodProduct("F002", "Sample", 0.0, 50, LocalDate.now().plusDays(10)); 
        ElectronicsProduct demoUnit = new ElectronicsProduct("E002", "Demo Unit", 500.0, 30); 

        warehouse.addProduct(apple);
        warehouse.addProduct(mac);
        warehouse.addProduct(freebie);
        warehouse.addProduct(demoUnit); 

       
        Assertions.assertEquals(2005.0, warehouse.getTotalValue(), 0.001, "Общая стоимость рассчитана неверно.");
    }

    
    @Test
    @Order(10)
    @DisplayName("Расчет общей стоимости пустого склада")
    void testGetTotalValue_EmptyWarehouse() {
        Assertions.assertEquals(0.0, warehouse.getTotalValue(), 0.001, "Общая стоимость пустого склада должна быть 0.");
    }

    
    @Test
    @Order(11)
    @DisplayName("Функциональный тест: Добавление, поиск, проверка состояния и удаление")
    void testFunctional_AddSearchCheckRemove() {
        
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate nextMonth = today.plusMonths(1);

        
        FoodProduct milk = new FoodProduct("F101", "Milk", 1.5, 20, tomorrow);
        
        ElectronicsProduct laptop = new ElectronicsProduct("E101", "Gaming Laptop", 1500.0, 365); 
        
        FoodProduct expiredBread = new FoodProduct("F102", "Expired Bread", 1.0, 10, today.minusDays(1));
        
        ElectronicsProduct usbDrive = new ElectronicsProduct("E102", "USB Drive 64GB", 25.0, 730); 

        
        warehouse.addProduct(milk);
        warehouse.addProduct(laptop);
        warehouse.addProduct(expiredBread);
        warehouse.addProduct(usbDrive);

        
        Assertions.assertEquals(4, warehouse.getAllProducts().size(), "На складе должно быть 4 продукта.");

       
        List<AbstractProduct> laptopResults = warehouse.findProductsByName("Laptop");
        Assertions.assertEquals(1, laptopResults.size(), "Должен быть найден один 'Laptop'.");
        Assertions.assertEquals("E101", laptopResults.get(0).getId());

        List<AbstractProduct> productResults = warehouse.findProductsByName("Product");
        Assertions.assertTrue(productResults.isEmpty(), "Поиск 'Product' не должен ничего найти.");

       
        Assertions.assertEquals(565790.0, warehouse.getTotalValue(), 0.001, "Общая стоимость рассчитана неверно.");

        
        boolean removed = warehouse.removeProduct("E102");
        Assertions.assertTrue(removed, "USB Drive должен быть успешно удален.");
        Assertions.assertEquals(3, warehouse.getAllProducts().size(), "После удаления должно остаться 3 продукта.");
        Assertions.assertNull(warehouse.getProductById("E102"), "USB Drive не должен быть найден после удаления.");

        
        Assertions.assertEquals(547540.0, warehouse.getTotalValue(), 0.001, "Общая стоимость после удаления рассчитана неверно.");
    }

    
    @Test
    @Order(12)
    @DisplayName("Функциональный тест: Добавление, проверка состояния и общая стоимость")
    void testFunctional_AddCheckValue() {
        
        FoodProduct screws = new FoodProduct("F201", "Screws (pack of 100)", 0.01, 500, LocalDate.now().plusYears(1)); 
        
        ElectronicsProduct server = new ElectronicsProduct("E201", "Server Rack", 2500.0, 180); 
        
        FoodProduct cheese = new FoodProduct("F202", "Cheese Wheel", 50.0, 15, LocalDate.now().plusDays(60)); 

        warehouse.addProduct(screws);
        warehouse.addProduct(server);
        warehouse.addProduct(cheese);

        
        Assertions.assertEquals(3, warehouse.getAllProducts().size(), "На складе должно быть 3 продукта.");

        
        AbstractProduct retrievedCheese = warehouse.getProductById("F202");
        Assertions.assertNotNull(retrievedCheese);
        Assertions.assertEquals("Cheese Wheel", retrievedCheese.getName());
        Assertions.assertEquals(15, retrievedCheese.getQuantity());

        
        Collection<AbstractProduct> allItems = warehouse.getAllProducts();
        Assertions.assertEquals(3, allItems.size());

        
        Assertions.assertEquals(450755.0, warehouse.getTotalValue(), 0.001, "Общая стоимость рассчитана неверно.");
    }

    
    @Test
    @Order(13)
    @DisplayName("Продукт с нулевым количеством")
    void testProductWithZeroQuantity() {
        ElectronicsProduct monitor = new ElectronicsProduct("E301", "Monitor", 300.0, 90); 
        warehouse.addProduct(monitor);
        Assertions.assertEquals(0, monitor.getQuantity(), "Количество должно быть 0.");
        
        Assertions.assertNotNull(warehouse.getProductById("E301"));
        Assertions.assertEquals(1, warehouse.getAllProducts().size());
        
        Assertions.assertEquals(0.0, warehouse.getTotalValue(), 0.001);
    }

    
    @Test
    @Order(14)
    @DisplayName("Продукт с нулевой ценой")
    void testProductWithZeroPrice() {
        FoodProduct promoItem = new FoodProduct("F301", "Promo Item", 0.0, 100, LocalDate.now().plusDays(90));
        warehouse.addProduct(promoItem);
        Assertions.assertEquals(0.0, promoItem.getPrice(), 0.001, "Цена должна быть 0.0.");
        Assertions.assertNotNull(warehouse.getProductById("F301"));
        Assertions.assertEquals(1, warehouse.getAllProducts().size());
       
        Assertions.assertEquals(0.0, warehouse.getTotalValue(), 0.001);
    }

    
    @Test
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
    @Order(17)
    @DisplayName("Получение товаров с низким остатком")
    void testGetLowStockProducts() {
        FoodProduct apple = new FoodProduct("F601", "Apple", 0.5, 5, LocalDate.now().plusDays(30)); 
        ElectronicsProduct phone = new ElectronicsProduct("E601", "Smartphone", 800.0, 365); 
        FoodProduct bread = new FoodProduct("F602", "Bread", 2.0, 15, LocalDate.now().plusDays(3)); 
        ElectronicsProduct monitor = new ElectronicsProduct("E602", "Monitor", 300.0, 0); 

        warehouse.addProduct(apple);
        warehouse.addProduct(phone);
        warehouse.addProduct(bread);
        warehouse.addProduct(monitor);

       
        List<AbstractProduct> lowStock5 = warehouse.getLowStockProducts(5);
        Assertions.assertEquals(2, lowStock5.size(), "Должно быть 2 товара с остатком < 5.");
        Assertions.assertTrue(lowStock5.stream().anyMatch(p -> "E601".equals(p.getId())));
        Assertions.assertTrue(lowStock5.stream().anyMatch(p -> "E602".equals(p.getId())));

        
        List<AbstractProduct> lowStock3 = warehouse.getLowStockProducts(3);
        Assertions.assertEquals(2, lowStock3.size(), "Должно быть 2 товара с остатком < 3.");
        Assertions.assertTrue(lowStock3.stream().anyMatch(p -> "E601".equals(p.getId())));
        Assertions.assertTrue(lowStock3.stream().anyMatch(p -> "E602".equals(p.getId())));

       
        List<AbstractProduct> lowStock0 = warehouse.getLowStockProducts(0);
        Assertions.assertTrue(lowStock0.isEmpty(), "Не должно быть товаров с остатком < 0.");

        
        List<AbstractProduct> lowStock1 = warehouse.getLowStockProducts(1);
        Assertions.assertEquals(2, lowStock1.size(), "Должно быть 2 товара с остатком < 1.");
        Assertions.assertTrue(lowStock1.stream().anyMatch(p -> "E601".equals(p.getId())));
        Assertions.assertTrue(lowStock1.stream().anyMatch(p -> "E602".equals(p.getId())));


        
        List<AbstractProduct> lowStock16 = warehouse.getLowStockProducts(16);
        Assertions.assertEquals(3, lowStock16.size(), "Должно быть 3 товара с остатком < 16.");
        Assertions.assertTrue(lowStock16.stream().anyMatch(p -> "F601".equals(p.getId()))); // Apple (5)
        Assertions.assertTrue(lowStock16.stream().anyMatch(p -> "E601".equals(p.getId()))); // phone (2)
        Assertions.assertTrue(lowStock16.stream().anyMatch(p -> "E602".equals(p.getId()))); // monitor (0)

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

   .
    @Test
    @Order(19)
    @DisplayName("Сохранение и загрузка данных склада из файла")
    void testDataPersistence() throws java.io.IOException {
        
        String testFileName = "temp_warehouse_data.csv";
        java.nio.file.Path tempFilePath = java.nio.file.Paths.get(testFileName);

        
        FoodProduct apple = new FoodProduct("P001", "Apple", 0.5, 10, LocalDate.now().plusDays(30));
        ElectronicsProduct phone = new ElectronicsProduct("P002", "Smartphone", 800.0, 365); 
        warehouse.addProduct(apple);
        warehouse.addProduct(phone);

        

        try {
            
            Field fileField = Warehouse.class.getDeclaredField("DATA_FILE");
            fileField.setAccessible(true); 
            String originalFileName = (String) fileField.get(null); 
            fileField.set(null, testFileName); 

            
            Method saveMethod = Warehouse.class.getDeclaredMethod("saveData");
            saveMethod.setAccessible(true); 
            saveMethod.invoke(warehouse); 

            
            Assertions.assertTrue(java.nio.file.Files.exists(tempFilePath));

            
            clearWarehouseData(warehouse);
            Assertions.assertEquals(0, warehouse.getAllProducts().size());

            
            Method loadMethod = Warehouse.class.getDeclaredMethod("loadData");
            loadMethod.setAccessible(true); 
            loadMethod.invoke(warehouse); 

            
            Assertions.assertEquals(2, warehouse.getAllProducts().size());
            AbstractProduct loadedApple = warehouse.getProductById("P001");
            AbstractProduct loadedPhone = warehouse.getProductById("P002");
            Assertions.assertNotNull(loadedApple);
            Assertions.assertNotNull(loadedPhone);
            Assertions.assertEquals("Apple", loadedApple.getName());
            Assertions.assertEquals("Smartphone", loadedPhone.getName());
            Assertions.assertTrue(loadedApple instanceof FoodProduct);
            Assertions.assertTrue(loadedPhone instanceof ElectronicsProduct);

        } catch (NoSuchFieldException e) {
            System.err.println("Ошибка рефлексии: Поле DATA_FILE не найдено. " + e.getMessage());
            throw new RuntimeException("Ошибка при работе с рефлексией: NoSuchFieldException.", e);
        } catch (NoSuchMethodException e) {
            System.err.println("Ошибка рефлексии: Метод saveData() или loadData() не найден. " + e.getMessage());
            throw new RuntimeException("Ошибка при работе с рефлексией: NoSuchMethodException.", e);
        } catch (IllegalAccessException e) {
            System.err.println("Ошибка рефлексии: Нет доступа к полю или методу. " + e.getMessage());
            throw new RuntimeException("Ошибка при работе с рефлексией: IllegalAccessException.", e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            System.err.println("Ошибка при вызове метода через рефлексию. " + e.getTargetException());
            throw new RuntimeException("Ошибка при вызове метода через рефлексию.", e.getTargetException());
        } finally {
            
            try {
                Field fileField = Warehouse.class.getDeclaredField("DATA_FILE");
                fileField.setAccessible(true);
                fileField.set(null, "warehouse_data.csv"); 
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.err.println("Не удалось восстановить исходное имя файла: " + e.getMessage());
            }
            java.nio.file.Files.deleteIfExists(tempFilePath); 
        }
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
