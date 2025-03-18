package ua.reed.utils;

public final class Constants {

    private Constants() {}

    public static final String PRODUCTS_TABLE_NAME = "products";
    public static final String STOCKS_TABLE_NAME = "stocks";
    public static final String PRODUCTS_TABLE_EXISTS_ID = "ProductsTableExists";
    public static final String STOCKS_TABLE_EXISTS_ID = "StocksTableExists";
    public static final String PRODUCTS_TABLE_ID = "ProductsTable";
    public static final String STOCKS_TABLE_ID = "StocksTable";
    public static final String STACK_ID = "AwsShopBackendStack";
    public static final String PRODUCT_TABLE_ENV_KEY = "PRODUCT_TABLE";
    public static final String STOCK_TABLE_ENV_KEY = "STOCK_TABLE";
    public static final String EXISTING_IMPORT_BUCKET_ID = "EXISTING_IMPORT_BUCKET";
    public static final String NEW_IMPORT_BUCKET_ID = "NEW_IMPORT_BUCKET";
    public static final String IMPORT_BUCKET_NAME_KEY = "IMPORT_BUCKET_NAME";
    public static final String IMPORT_BUCKET_NAME = "rs-school-task-5-imports";
    public static final String CSV_FILENAME_KEY = "name";
    public static final int DEFAULT_PRE_SIGNED_URL_DURATION_MINUTES = 10;
    public static final String IMPORT_FILE_PATH = "import";
    public static final String UPLOAD_S3_DIRECTORY = "uploaded/";
    public static final String PARSED_S3_DIRECTORY = "parsed/";
    public static final String CATALOG_ITEMS_QUEUE_ID = "catalogItemsQueueId";
    public static final String CATALOG_ITEMS_QUEUE_NAME = "catalog-items-queue";
    public static final String CATALOG_ITEMS_QUEUE_KEY = "CATALOG_ITEMS_QUEUE_KEY";

}
