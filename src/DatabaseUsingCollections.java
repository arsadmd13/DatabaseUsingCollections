import java.util.*;

class Database {
    static String DBNAME = null;
    static String TABLENAME = null;
    static TreeSet<String> dbNames = new TreeSet<>();
    static TreeSet<String> tableNames = new TreeSet<>();
    static LinkedHashMap<String, TreeSet<String>> tableNamesData = new LinkedHashMap<>();
    static HashMap<String, LinkedHashMap<String, HashMap<String, String>>> columnData = new HashMap<>();
    static HashMap<String, LinkedList<ArrayList<String>>> rowData = new HashMap<>();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String...args) {
        showMainMenu();
    }

    public static void showMainMenu() {
        int option;
        do {
            System.out.print("""
                    ____________->MENU<-______________
                    1.Create DB
                    2.Select DB
                    3.Create Table
                    4.Show current db
                    5.Show databases
                    6.Show tables
                    7.Show table structure
                    8.Add row
                    9.Select row
                    10.Update row
                    11.Delete row
                    12.PreSetDb
                    13.General Info
                    14.Exit

                    Choose an option:\s""");
            option = scanner.nextInt();
            scanner.nextLine();
            switch (option) {
                case 1 -> createDb();
                case 2 -> selectDb();
                case 3 -> createTable();
                case 4 -> getCurrentDb();
                case 5 -> showDatabases();
                case 6 -> showTables();
                case 7 -> showTableStructure();
                case 8 -> addRow();
                case 9 -> selectRow();
                case 10 -> updateRow();
                case 11 -> deleteRow();
                case 12 -> preSetDb();
                case 13 -> showCustomInfo();
                case 14 -> System.out.println("Bye :)");
            }
        } while(option < 14);
    }

    public static void createDb() {
        System.out.print("Enter db name: ");
        String dbName = scanner.nextLine();
        if(!isValidName(dbName)) {
            System.out.println("The name you entered is not valid. Please do not include any special characters except '_'.\nTry again!");
            createDb();
            return;
        }
        if(dbNames.contains(dbName)) {
            System.out.println("The name you entered already exists. Please try again with a different name!");
            return;
        }
        // DBNAME = dbName;
        dbNames.add(dbName);
        tableNamesData.put(dbName, new TreeSet<>());
        System.out.print("Db created successfully!\nDo you want this db to be set as your current db? (y/N): ");
        char choice =  scanner.next().charAt(0);
        if(choice == 'y' || choice == 'Y') {
            System.out.println("Db created and has been set as current db.");
            DBNAME = dbName;
        } else {
            System.out.println("DB Created");
        }
    }

    public static void selectDb() {
        System.out.print("Enter db name: ");
        String dbName = scanner.nextLine();
        if(dbNames.contains(dbName)) {
            DBNAME = dbName;
            tableNames = tableNamesData.get(dbName);
            System.out.println("Db has been set successfully!");
        } else {
            System.out.println("The db you entered doesn't exist!");
        }
    }

    public static void getCurrentDb() {
        if(!isDbSelected()) return;
        System.out.println("Current DB: " + DBNAME);
    }

    public static void showTables() {
        if(!isDbSelected()) return;
        TreeSet<String> tempTableNames = tableNamesData.get(DBNAME);
        if(tempTableNames.size() == 0) {
            System.out.println("This db has no tables!");
            return;
        }
        for (String tempTableName : tempTableNames) {
            System.out.println(tempTableName);
        }
    }

    public static void showDatabases() {
        if(dbNames.size() == 0) {
            System.out.println("No db available!");
            return;
        }
        for (String dbName : dbNames) {
            System.out.println(dbName);
        }
    }

    public static void showTableStructure() {
        if(!isDbSelected()) return;
        System.out.print("Enter table name: ");
        String tableName = scanner.nextLine();
        if(columnData.containsKey(DBNAME + "@" +tableName)) {
            LinkedHashMap<String, HashMap<String, String>> columns = columnData.get(DBNAME + "@" +tableName);
            showColumns(null, columns);
        } else {
            System.out.println("Table doesn't exist!");
        }
    }

    public static boolean isDbSelected() {
        if(DBNAME != null) {
            return true;
        }
        System.out.println("Please choose db first!");
        return false;
    }

    public static void createTable() {
        if(!isDbSelected()) return;
        int option;
        boolean endMenu = false;
        System.out.print("Enter table name: ");
        String tableName = scanner.nextLine();
        if(!isValidName(tableName)) {
            System.out.println("The name you entered is not valid. Please do not include any special characters except '_'.\nTry again!");
            createTable();
            return;
        }
        if(tableNamesData.get(DBNAME).contains(tableName)) {
            System.out.println("A table with the same name already exists!.\nTry again!");
            return;
        }
        ArrayList<String> columns = new ArrayList<>();
        do {
            System.out.print("""
                    _______->OPTIONS<-_______
                    1.Add column
                    2.Update column
                    3.Delete column
                    4.Show added columns
                    5.Finish creating table
                    6.Discard table creation

                    Choose an option:\s""");
            option = scanner.nextInt();
            scanner.nextLine();
            switch(option) {
                case 1:
                    addColumn(columns, false, null);
                    break;
                case 2:
                    updateColumn(columns);
                    break;
                case 3:
                    deleteColumn(columns);
                    break;
                case 4:
                    showColumns(columns, null);
                    break;
                case 5:
                    if(columns.size() > 0)  {
                        endMenu = true;
                        finishCreatingTable(tableName, columns);
                    } else {
                        System.out.println("Add atleast one column before finishing.");
                    }
                    break;
            }
            if(endMenu)
                break;
        } while(option < 6);

    }

    public static void addColumn(ArrayList<String> columns, boolean replace, String oldVal) {
        System.out.print("Enter column name: ");
        String columnName = scanner.nextLine();
        if(!isValidName(columnName)) {
            System.out.println("The name you entered is not valid. Please do not include any special characters except '_'.\nTry again!");
        } else {
            boolean check = columns.stream().filter(tempColumnName -> tempColumnName.split("@")[0].equals(columnName)).findFirst().isEmpty();
            if(!check && !replace) {
                System.out.println("The column name you entered already exists!");
                return;
            } else if(!check && replace) {
                if(!columns.get(columns.indexOf(oldVal)).split("@")[0].equals(columnName)) {
                    System.out.println("The column name you entered already exists!");
                    return;
                }
            }
            System.out.print("Enter column data type [accepted type: varchar, integer, text] : ");
            String columnDataType = scanner.nextLine();
            int lengthOfData;
            String finalStr = columnName + "@" + columnDataType;
            if(columnDataType.equalsIgnoreCase("varchar") || columnDataType.equalsIgnoreCase("integer")) {
                System.out.print("Enter the maximum length of the data: ");
                lengthOfData = scanner.nextInt();
                scanner.nextLine();
                finalStr += "@" + lengthOfData;
            } else if(columnDataType.equalsIgnoreCase("text")) {
                //Do Nothin
            } else {
                System.out.println("Invalid column data type.\nPlease head over to info section in the main menu to learn more about accepted column data types.");
                return;
            }
            System.out.print("Allow null ? (y/Any other Key): ");
            char nullResponse = scanner.next().charAt(0);
            if(nullResponse == 'y' || nullResponse == 'Y') {
                finalStr += "@" + "N";
            } else {
                finalStr += "@" + "NN";
            }
            if(replace) {
                columns.set(columns.indexOf(oldVal), finalStr);
                System.out.println("Column has been updated successfully!");
            } else {
                columns.add(finalStr);
                System.out.println("Column has been added successfully!");
            }
        }
    }

    public static void updateColumn(ArrayList<String> columns) {
        System.out.print("Enter the column name to update: ");
        String columnName = scanner.nextLine();
        String columnNameFromSet = columns.stream().filter(tempColumnName -> tempColumnName.split("@")[0].equals(columnName)).findFirst().get();
        if(columnNameFromSet.contains(columnName)) {
            addColumn(columns, true, columnNameFromSet);
        } else {
            System.out.println("Column not found!");
        }
    }

    public static void deleteColumn(ArrayList<String> columns) {
        System.out.print("Enter the column name to remove: ");
        String columnName = scanner.nextLine();
        boolean result = columns.removeIf(tempColumnName -> tempColumnName.split("@")[0].equals(columnName));
        if(result) {
            System.out.println("Column has been removed successfully!");
        } else {
            System.out.println("Unable to complete the request! Possible err - column doesn't exist");
        }
    }

    public static void showColumns(ArrayList<String> alColumns, LinkedHashMap<String, HashMap<String, String>> lhsColumns) {
        if(lhsColumns == null) {
            for (String alColumn : alColumns) {
                String[] columnData = alColumn.split("@");
                if (columnData.length == 3) {
                    System.out.println("Column Name: " + columnData[0] + " - Column  Data Type: " + columnData[1] + " - Maximum  Data Length: " + columnData[2]);
                } else {
                    System.out.println("Column Name: " + columnData[0] + " - Column  Data Type: " + columnData[1]);
                }
            }
        } else {
            for(Map.Entry<String, HashMap<String, String>> columnDetail: lhsColumns.entrySet()) {
                HashMap<String, String> columnConfig = columnDetail.getValue();
                if(!columnConfig.containsValue("text")) {
                    System.out.println("Column Name: " + columnConfig.get("columnName") + " - Column Data Type: " + columnConfig.get("columnDatatype") + " - Maximum Data Length: " + columnConfig.get("columnLength") + " - Allow Null: " + columnConfig.get("allowNull"));
                } else {
                    System.out.println("Column Name: " + columnConfig.get("columnName") + " - Column Data Type: " + columnConfig.get("columnDatatype") + " - Maximum Data Length: NA - Allow Null: " + columnConfig.get("allowNull"));
                }
            }
        }
    }

    public static void finishCreatingTable(String tableName, ArrayList<String> columns) {
        System.out.println("Adding table...");
        tableNamesData.get(DBNAME).add(tableName);
        LinkedHashMap<String, HashMap<String, String>> tempColumnsMap = new LinkedHashMap<>();
        for (String columnDetail : columns) {
            HashMap<String, String> tempHashMap = new HashMap<>();
            tempHashMap.put("columnName", columnDetail.split("@")[0]);
            tempHashMap.put("columnDatatype", columnDetail.split("@")[1]);
            tempHashMap.put("columnLength", columnDetail.split("@")[2]);
            tempHashMap.put("allowNull", columnDetail.split("@")[3]);
            tempColumnsMap.put(columnDetail.split("@")[0], tempHashMap);
        }
        columnData.put(DBNAME + "@" + tableName, tempColumnsMap);
        rowData.put(DBNAME + "@" + tableName, new LinkedList<>());
        System.out.println("Table has been added successfully!");
    }

    public static void addRow() {
        if(!isDbSelected()) return;
        System.out.print("Enter table name: ");
        String tableName = scanner.nextLine();
        String tableKey = DBNAME + "@" +tableName;
        if(columnData.containsKey(tableKey)) {
            LinkedHashMap<String, HashMap<String, String>> columns = columnData.get(DBNAME + "@" +tableName);
            ArrayList<String> tempRowData = new ArrayList<>();
            for (Map.Entry<String, HashMap<String, String>> columnEntry: columns.entrySet()) {
                HashMap<String, String> tempColumnData = columnEntry.getValue();//columnsIterator.next();
                String data;
                while (true){
                    data = "";
                    System.out.print("Enter data for column '" + tempColumnData.get("columnName") + "': ");
                        data += scanner.nextLine();
                    if(data.isEmpty()) {
                        data = null;
                    }
                    if(data != null && !data.equals("--Quit--") && (tempColumnData.get("columnDatatype").equalsIgnoreCase("integer"))) {
                        try {
                            Integer.parseInt(data);
                            if(data.length() > Integer.parseInt(tempColumnData.get("columnLength"))) {
                                System.out.println("Data length is longer than expected [Expected Length: " + tempColumnData.get("columnLength") + "]. Try again or enter '--Quit--' to quit.");
                            } else {
                                break;
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("Datatype mismatch [Expected integer type]. Try again or enter '--Quit--' to quit.");
                        }
                    } else if(data != null && !data.equals("--Quit--") && tempColumnData.get("columnDatatype").equalsIgnoreCase("varchar") &&
                            data.length() > Integer.parseInt(tempColumnData.get("columnLength"))) {
                        System.out.println("Data length is longer than expected [Expected Length: " + tempColumnData.get("columnLength") + "]. Try again or enter '--Quit--' to quit.");
                    } else if(data == null && tempColumnData.get("allowNull").equals("Yes")) {
                        System.out.println("This field doesn't support null value. Try again or enter '--Quit--' to quit.");
                    } else {
                        break;
                    }
                }
                if(data != null && data.equals("--Quit--")) {
                    return;
                }
                tempRowData.add(data);
            }
            LinkedList<ArrayList<String>> mainRowData = rowData.get(tableKey);
            mainRowData.add(tempRowData);
            rowData.put(tableKey, mainRowData);
            System.out.println("Row has been added successfully!");
        } else {
            System.out.println("Table doesn't exist!");
        }
    }

    public static void selectRow() {
        if(!isDbSelected()) return;
        System.out.print("Enter table name: ");
        String tableName = scanner.nextLine();
        TABLENAME = tableName;
        String tableKey = DBNAME + "@" +tableName;
        if(columnData.containsKey(tableKey)) {
            LinkedList<ArrayList<String>> mainRowData = rowData.get(tableKey);
            LinkedHashMap<String, HashMap<String, String>> columns = columnData.get(tableKey);
            ArrayList<String> columnsToSelect = new ArrayList<>();
            ArrayList<String> columnsConditions = new ArrayList<>();
            if(mainRowData.size() > 0) {
                int queryOption;
                do {
                    System.out.print("""
                            ________________->QUERY BUILDER<-________________
                            1.Add column to select
                            2.Add column condition
                            3.Proceed

                            Choose an option:\s""");
                    queryOption = scanner.nextInt();
                    scanner.nextLine();
                    switch (queryOption) {
                        case 1 -> addColumnToSelect(columnsToSelect, columns, false);
                        case 2 -> addColumnCondition(columnsConditions, columns);
                    }
                } while(queryOption < 3);
                if(columnsToSelect.size() == 0) {
                    addColumnToSelect(columnsToSelect, columns, true);
                }
            } else {
                System.out.println("No rows present in this table!");
                return;
            }
            long queryStartTime = System.currentTimeMillis();
            Iterator<String> columnsToSelectIterator = columnsToSelect.iterator();
            Iterator<String> columnConditionsIterator;
            while(columnsToSelectIterator.hasNext()) {
                String columnsToSelectDetails = columnsToSelectIterator.next();
                if(columnsToSelectDetails.equals("*")) continue;
                System.out.print(columnsToSelectDetails.split("@")[0] + "\t\t");
            }
            System.out.println();
            Iterator<ArrayList<String>> mainRowIterator = mainRowData.iterator();
            int totalRowsFetched = 0;
            while(mainRowIterator.hasNext()) {
                columnConditionsIterator = columnsConditions.iterator();
                ArrayList<String> tempArrayList = mainRowIterator.next();
                boolean skip = false;
//                System.out.println(columnsConditions.size());
                while (columnConditionsIterator.hasNext()) {
                    String columnConditionDetail = columnConditionsIterator.next();
//                    /*if(totalRowsFetched == 1) */System.out.println(columnConditionDetail);
                    int columnIndex = getColumnIndex(TABLENAME, columnConditionDetail.split("@")[0]);
                    String conditionControl = columnConditionDetail.split("@")[1];
                    String conditionString = columnConditionDetail.split("@")[2];
                    if(conditionControl.equals("contains") && !tempArrayList.get(columnIndex).contains(conditionString)) {
                        skip = true;
                    }
                    if(conditionControl.equals("equals") && !tempArrayList.get(columnIndex).equals(conditionString)) {
                        skip = true;
                    }
                    if(conditionControl.equals("greater") && !(Integer.parseInt(tempArrayList.get(columnIndex)) > Integer.parseInt(conditionString))) {
                        skip = true;
                    }
                    if(conditionControl.equals("lesser") && !(Integer.parseInt(tempArrayList.get(columnIndex)) < Integer.parseInt(conditionString))) {
                        skip = true;
                    }
                    if(conditionControl.equals("equalsTo") && !(Integer.parseInt(tempArrayList.get(columnIndex)) == Integer.parseInt(conditionString))) {
                        skip = true;
                    }
                }
                if(skip) {
                    continue;
                }
                columnsToSelectIterator = columnsToSelect.iterator();
                while(columnsToSelectIterator.hasNext()) {
                    String columnsToSelectDetails = columnsToSelectIterator.next();
                    if(columnsToSelectDetails.equals("*")) continue;
                    System.out.print(tempArrayList.get(Integer.parseInt(columnsToSelectDetails.split("@")[1]))  + "\t\t");
                }
                System.out.println();
                totalRowsFetched++;
            }
            long timeTakenForQueryToComplete = System.currentTimeMillis() - queryStartTime;
            if(totalRowsFetched == 0) {
                System.out.println("No matching records found!");
            } else {
                System.out.println("Total " + totalRowsFetched + " row(s) fetched in " + timeTakenForQueryToComplete + "ms");
            }
        } else {
            System.out.println("Table doesn't exist!");
        }
    }

    public static void updateRow() {
        if(!isDbSelected()) return;
        System.out.print("Enter table name: ");
        String tableName = scanner.nextLine();
        TABLENAME = tableName;
        String tableKey = DBNAME + "@" +tableName;
        if(columnData.containsKey(tableKey)) {
            LinkedList<ArrayList<String>> mainRowData = rowData.get(tableKey);
            LinkedHashMap<String, HashMap<String, String>> columns = columnData.get(tableKey);
            ArrayList<String> columnsToUpdate = new ArrayList<>();
            ArrayList<String> columnsConditions = new ArrayList<>();
            if(mainRowData.size() > 0) {
                int queryOption;
                do {
                    System.out.print("""
                            ________________->QUERY BUILDER<-________________
                            1.Add column to update
                            2.Add column condition
                            3.Proceed

                            Choose an option:\s""");
                    queryOption = scanner.nextInt();
                    scanner.nextLine();
                    switch(queryOption) {
                        case 1:
                            addColumnToUpdate(columnsToUpdate, columns);
                            break;
                        case 2:
                            addColumnCondition(columnsConditions, columns);
                            break;
                    }
                } while(queryOption < 3);
                if(columnsToUpdate.size() == 0) {
                    System.out.println("No column(s) has been selected to update!");
                    return;
                }
            } else {
                System.out.println("No rows present in this table!");
                return;
            }
            long queryStartTime = System.currentTimeMillis();
            Iterator<String> columnsToUpdateIterator;// = columnsToUpdate.iterator();
            Iterator<String> columnConditionsIterator;
            Iterator<ArrayList<String>> mainRowIterator = mainRowData.iterator();
            int totalRowsUpdated = 0;
            while(mainRowIterator.hasNext()) {
                columnConditionsIterator = columnsConditions.iterator();
                ArrayList<String> tempArrayList = mainRowIterator.next();
                boolean skip = false;
                while (columnConditionsIterator.hasNext()) {
                    String columnConditionDetail = columnConditionsIterator.next();
                    int columnIndex = getColumnIndex(TABLENAME, columnConditionDetail.split("@")[0]);
                    String conditionControl = columnConditionDetail.split("@")[1];
                    String conditionString = columnConditionDetail.split("@")[2];
                    if(conditionControl.equals("contains") && !tempArrayList.get(columnIndex).contains(conditionString)) {
                        skip = true;
                    }
                    if(conditionControl.equals("equals") && !tempArrayList.get(columnIndex).equals(conditionString)) {
                        skip = true;
                    }
                    if(conditionControl.equals("greater") && !(Integer.parseInt(tempArrayList.get(columnIndex)) > Integer.parseInt(conditionString))) {
                        skip = true;
                    }
                    if(conditionControl.equals("lesser") && !(Integer.parseInt(tempArrayList.get(columnIndex)) < Integer.parseInt(conditionString))) {
                        skip = true;
                    }
                    if(conditionControl.equals("equalsTo") && !(Integer.parseInt(tempArrayList.get(columnIndex)) == Integer.parseInt(conditionString))) {
                        skip = true;
                    }
                }
                if(skip) {
                    continue;
                }
                columnsToUpdateIterator = columnsToUpdate.iterator();
                while(columnsToUpdateIterator.hasNext()) {
                    String columnsToUpdateDetails = columnsToUpdateIterator.next();
                    tempArrayList.set(Integer.parseInt(columnsToUpdateDetails.split("@")[1]), columnsToUpdateDetails.split("@")[2]);
                }
                totalRowsUpdated++;
            }
            long timeTakenForQueryToComplete = System.currentTimeMillis() - queryStartTime;
            if(totalRowsUpdated == 0) {
                System.out.println("No matching records found!");
            } else {
                System.out.println("Total " + totalRowsUpdated + " row(s) updated in " + timeTakenForQueryToComplete + "ms");
            }
        } else {
            System.out.println("Table doesn't exist!");
        }
    }

    public static void deleteRow() {
        if(!isDbSelected()) return;
        System.out.print("Enter table name: ");
        String tableName = scanner.nextLine();
        TABLENAME = tableName;
        String tableKey = DBNAME + "@" +tableName;
        if(columnData.containsKey(tableKey)) {
            LinkedList<ArrayList<String>> mainRowData = rowData.get(tableKey);
            LinkedHashMap<String, HashMap<String, String>> columns = columnData.get(tableKey);
            ArrayList<String> columnsConditions = new ArrayList<>();
            if(mainRowData.size() > 0) {
                int queryOption;
                do {
                    System.out.print("""
                            ________________->QUERY BUILDER<-________________
                            1.Add column condition
                            2.Proceed

                            Choose an option:\s""");
                    queryOption = scanner.nextInt();
                    scanner.nextLine();
                    switch (queryOption) {
                        case 1 -> addColumnCondition(columnsConditions, columns);
                    }
                } while(queryOption < 3);
            } else {
                System.out.println("No rows present in this table!");
                return;
            }
            long queryStartTime = System.currentTimeMillis();
            Iterator<String> columnConditionsIterator;
            Iterator<ArrayList<String>> mainRowIterator = mainRowData.iterator();
            int totalRowsRemoved = 0;
            while(mainRowIterator.hasNext()) {
                columnConditionsIterator = columnsConditions.iterator();
                ArrayList<String> tempArrayList = mainRowIterator.next();
                boolean skip = false;
                while (columnConditionsIterator.hasNext()) {
                    String columnConditionDetail = columnConditionsIterator.next();
                    int columnIndex = getColumnIndex(TABLENAME, columnConditionDetail.split("@")[0]);
                    String conditionControl = columnConditionDetail.split("@")[1];
                    String conditionString = columnConditionDetail.split("@")[2];
                    if(conditionControl.equals("contains") && !tempArrayList.get(columnIndex).contains(conditionString)) {
                        skip = true;
                    }
                    if(conditionControl.equals("equals") && !tempArrayList.get(columnIndex).equals(conditionString)) {
                        skip = true;
                    }
                    if(conditionControl.equals("greater") && !(Integer.parseInt(tempArrayList.get(columnIndex)) > Integer.parseInt(conditionString))) {
                        skip = true;
                    }
                    if(conditionControl.equals("lesser") && !(Integer.parseInt(tempArrayList.get(columnIndex)) < Integer.parseInt(conditionString))) {
                        skip = true;
                    }
                    if(conditionControl.equals("equalsTo") && !(Integer.parseInt(tempArrayList.get(columnIndex)) == Integer.parseInt(conditionString))) {
                        skip = true;
                    }
                }
                if(skip) {
                    continue;
                }
                mainRowIterator.remove();
                totalRowsRemoved++;
            }
            long timeTakenForQueryToComplete = System.currentTimeMillis() - queryStartTime;
            if(totalRowsRemoved == 0) {
                System.out.println("No matching records found!");
            } else {
                System.out.println("Total " + totalRowsRemoved + " row(s) removed in " + timeTakenForQueryToComplete + "ms");
            }
        } else {
            System.out.println("Table doesn't exist!");
        }
    }

    public static void addColumnToSelect(ArrayList<String> columnsToSelect, LinkedHashMap<String, HashMap<String, String>> columnsAvailable, boolean addAll) {
        String columnName;
        if(addAll) {
            columnsToSelect.add("*");
            for (String columnKey : columnsAvailable.keySet()) {
                columnsToSelect.add(columnKey + "@" + getColumnIndex(TABLENAME, columnKey));
            }
        } else {
            System.out.print("Enter column name [Enter '*' for all columns]: ");
            columnName = scanner.nextLine();
            if(!columnName.equals("*") && !columnsAvailable.containsKey(columnName)) {
                System.out.println("Invalid column name!");
            } else if(columnsToSelect.size() > 0) {
                if (columnName.equals("*")) {
                    System.out.println("You cannot add '*' wildcard after adding a column!");
                } else if (columnsToSelect.get(0).contains("*")) {
                    System.out.println("You cannot add a column after adding '*' wildcard!");
                } else if (!columnsToSelect.get(0).contains("*")) {
                    columnsToSelect.add(columnName + "@" + getColumnIndex(TABLENAME, columnName));
                }
            } else {
                if (columnName.equals("*")) {
                    columnsToSelect.add("*");
                    for (String columnKey : columnsAvailable.keySet()) {
                        columnName = columnKey;
                        columnsToSelect.add(columnName + "@" + getColumnIndex(TABLENAME, columnName));
                    }
                } else {
                    columnsToSelect.add(columnName + "@" + getColumnIndex(TABLENAME, columnName));
                }
            }
        }
    }

    public static void addColumnToUpdate(ArrayList<String> columnsToSelect, LinkedHashMap<String, HashMap<String, String>> columnsAvailable) {
        System.out.print("Enter column name: ");
        String columnName = scanner.nextLine();
        if(!columnsAvailable.containsKey(columnName)) {
            System.out.println("Invalid column name!");
        } else {
            System.out.print("Enter new value to replace with: ");
            String newValue = scanner.nextLine();
            columnsToSelect.add(columnName + "@" + getColumnIndex(TABLENAME, columnName) + "@" + newValue);
        }
    }

    public static void addColumnCondition(ArrayList<String> columnsConditions, LinkedHashMap<String, HashMap<String, String>> columnsAvailable) {
        System.out.print("Enter column name: ");
        String columnName = scanner.nextLine();
        if(!columnsAvailable.containsKey(columnName)) {
            System.out.println("Invalid column name!");
        } else {
            HashMap<String, String> columnDetails = columnsAvailable.get(columnName);
            String condition = "";
            if(columnDetails.get("columnDatatype").equals("varchar") || columnDetails.get("columnDatatype").equals("text")) {
                System.out.print("""
                        _______->CONDITIONS MENU<-______
                        1.Contains
                        2.Equals
                        3.Discard

                        Choose an option:\s""");
                int optionNumber = scanner.nextInt();
                scanner.nextLine();
                switch (optionNumber) {
                    case 1 -> {
                        System.out.print("Contains: ");
                        condition = columnName + "@contains@" + scanner.nextLine();
                    }
                    case 2 -> {
                        System.out.print("Equals: ");
                        condition = columnName + "@equals@" + scanner.nextLine();
                    }
                }
            } else {
                System.out.print("""
                        _______->CONDITIONS MENU<-______
                        1.Greater than
                        2.Lesser than
                        3.Equals to
                        4.Discard

                        Choose an option:\s""");
                int optionNumber = scanner.nextInt();
                scanner.nextLine();
                switch (optionNumber) {
                    case 1 -> {
                        System.out.print("Greater than: ");
                        condition = columnName + "@greater@" + scanner.nextInt();
                    }
                    case 2 -> {
                        System.out.print("Lesser than: ");
                        condition = columnName + "@lesser@" + scanner.nextInt();
                    }
                    case 3 -> {
                        System.out.print("Equals to: ");
                        condition = columnName + "@equalsTo@" + scanner.nextInt();
                    }
                }
            }
            columnsConditions.add(condition);
        }
    }

    public static void preSetDb() {
        DBNAME = "EmployeeDb";
        String tableName = "EmployeeTable";
        dbNames.add(DBNAME);
        tableNamesData.put(DBNAME, new TreeSet<>());
        tableNamesData.get(DBNAME).add(tableName);
        LinkedHashMap<String, HashMap<String, String>> tempColumnSet = new LinkedHashMap<>();
        HashMap<String, String> columnDataMap;
        columnDataMap = new HashMap<>();
        columnDataMap.put("columnName", "Id");
        columnDataMap.put("columnDatatype", "integer");
        columnDataMap.put("columnLength", "2");
        columnDataMap.put("allowNull", "No");
        tempColumnSet.put("Id", new HashMap<>(columnDataMap));
        columnDataMap.clear();
        columnDataMap.put("columnName", "Name");
        columnDataMap.put("columnDatatype", "varchar");
        columnDataMap.put("columnLength", "100");
        columnDataMap.put("allowNull", "No");
        tempColumnSet.put("Name", new HashMap<>(columnDataMap));
        columnDataMap.clear();
        columnDataMap.put("columnName", "Salary");
        columnDataMap.put("columnDatatype", "integer");
        columnDataMap.put("columnLength", "10");
        columnDataMap.put("allowNull", "No");
        tempColumnSet.put("Salary", new HashMap<>(columnDataMap));
        columnDataMap.clear();
        columnDataMap.put("columnName", "Designation");
        columnDataMap.put("columnDatatype", "varchar");
        columnDataMap.put("columnLength", "100");
        columnDataMap.put("allowNull", "No");
        tempColumnSet.put("Designation", new HashMap<>(columnDataMap));
        columnDataMap.clear();
        columnDataMap.put("columnName", "Reporting_Officer");
        columnDataMap.put("columnDatatype", "varchar");
        columnDataMap.put("columnLength", "100");
        columnDataMap.put("allowNull", "Yes");
        tempColumnSet.put("Reporting_Officer", new HashMap<>(columnDataMap));
        columnData.put(DBNAME + "@" + tableName, tempColumnSet);
        LinkedList<ArrayList<String>> mainRowData = new LinkedList<>();
        int[] ids = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        String[] names = {"Louis", "Stark", "Romanoff", "Rogers", "Holland", "Pratt", "Hemsworth", "Hawkeye", "Banner"};
        String[] salaries = {"25000", "90000", "100000", "75000", "97000", "44000", "62000", "29000", "150000"};
        String[] designations = {"SDE Intern", "Senior Developer", "Support Lead", "Customer Relations Head", "Chief Engineer", "Design Lead", "Software Engineer", "Financial Advisor", "Testing Engineer"};
        String[] reportingOfficers = {"Tom", "Bricks", "Tony", "Scarlett", "Steve", "Peter", "Star Lord", "Chris", "Damon"};
        ArrayList<String> tempRowData;
        for(int i = 0 ; i < ids.length; i++) {
            tempRowData = new ArrayList<>();
            tempRowData.add(ids[i] + "");
            tempRowData.add(names[i]);
            tempRowData.add(salaries[i]);
            tempRowData.add(designations[i]);
            tempRowData.add(reportingOfficers[i]);
            mainRowData.add(tempRowData);
        }
        rowData.put(DBNAME + "@" + tableName, new LinkedList<>());
        rowData.put(DBNAME + "@" + tableName, mainRowData);
        System.out.println("Table has been added successfully!");
    }

    public static boolean isValidName(String name) {
        return !name.contains(" ") && !name.contains("+") && !name.contains("!") && !name.contains("@") && !name.contains("#") &&
                !name.contains("$") && !name.contains("%") && !name.contains("^") && !name.contains("&") && !name.contains("*") &&
                !name.contains("(") && !name.contains(")") && !name.contains("-") && !name.contains("/") && !name.contains(">") &&
                !name.contains("<") && !name.contains(";") && !name.contains(":") && !name.contains("'") && !name.contains("\"") &&
                !name.contains("|") && !name.contains("}") && !name.contains("{") && !name.contains("[") && !name.contains("]");
    }

    public static int getColumnIndex(String tableName, String columnName) {
        LinkedHashMap<String, HashMap<String, String>> columns = columnData.get(DBNAME + "@" +tableName);
        return new ArrayList<>(columns.keySet()).indexOf(columnName);
    }

    public static  void showCustomInfo() {
        System.out.println("Accepted Datatype: Varchar, Integer, Text\nVarchar, integer requires length for the field.\nUse the PreSetDb option (12) in the main menu  to automatically implement a test db (EmployeeDb), table (EmployeeTable) - columns: Id(Integer-2), Name(Varchar-100), Salary(Integer-10), Designation(Varchar-100), Reporting_Officer(Varchar-100) and some sample data.");
    }
}