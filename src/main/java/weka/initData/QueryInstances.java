package weka.initData;

import weka.core.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Date;
import java.util.*;

/**
 * @ClassName QueryInstances
 * @Description 数据库转换为Instances
 * @Author 林春永
 * @Date 2020/1/25
 * @Version 1.0
 **/
public class QueryInstances {


    private static String PROPERTY_FILE = "DatabaseUtils.props";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    String username = "";

    String password = "";

    String query = "";

    public String getDatabaseURL() {
        return DatabaseURL;
    }

    public void setDatabaseURL(String databaseURL) {
        DatabaseURL = databaseURL;
    }

    String DatabaseURL = "";
    /**
     * The name of the table containing the index to experiments.
     */
    public static final String EXP_INDEX_TABLE = "Experiment_index";

    /**
     * The name of the column containing the experiment type (ResultProducer).
     */
    public static final String EXP_TYPE_COL = "Experiment_type";

    /**
     * The name of the column containing the experiment setup (parameters).
     */
    public static final String EXP_SETUP_COL = "Experiment_setup";

    /**
     * The name of the column containing the results table name.
     */
    public static final String EXP_RESULT_COL = "Result_table";

    /**
     * The prefix for result table names.
     */
    public static final String EXP_RESULT_PREFIX = "Results";
    private static boolean sparseData = false;

    /**
     * For databases where Tables and Columns are created in upper case.
     */
    protected static boolean m_checkForUpperCaseNames = false;

    /**
     * For databases where Tables and Columns are created in lower case.
     */
    protected static boolean m_checkForLowerCaseNames = false;


    /* Type mapping used for reading experiment results */
    /**
     * Type mapping for STRING used for reading experiment results.
     */
    public static final int STRING = 0;
    /**
     * Type mapping for BOOL used for reading experiment results.
     */
    public static final int BOOL = 1;
    /**
     * Type mapping for DOUBLE used for reading experiment results.
     */
    public static final int DOUBLE = 2;
    /**
     * Type mapping for BYTE used for reading experiment results.
     */
    public static final int BYTE = 3;
    /**
     * Type mapping for SHORT used for reading experiment results.
     */
    public static final int SHORT = 4;
    /**
     * Type mapping for INTEGER used for reading experiment results.
     */
    public static final int INTEGER = 5;
    /**
     * Type mapping for LONG used for reading experiment results.
     */
    public static final int LONG = 6;
    /**
     * Type mapping for FLOAT used for reading experiment results.
     */
    public static final int FLOAT = 7;
    /**
     * Type mapping for DATE used for reading experiment results.
     */
    public static final int DATE = 8;
    /**
     * Type mapping for TEXT used for reading, e.g., text blobs.
     */
    public static final int TEXT = 9;
    /**
     * Type mapping for TIME used for reading TIME columns.
     */
    public static final int TIME = 10;
    /**
     * Type mapping for TIMESTAMP used for reading java.sql.Timestamp columns
     */
    public static final int TIMESTAMP = 11;
    //读取数据配置文件
    static Properties properties;
    Connection connection;

    Vector<String> columnNames;

    public Vector<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(Vector<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<Map<String, Object>> getQueryList() {
        return queryList;
    }

    public void setQueryList(List<Map<String, Object>> queryList) {
        this.queryList = queryList;
    }

    List<Map<String, Object>> queryList;

    public QueryInstances() {

        properties = new Properties();
//        BufferedReader bufferedReader = null;
        InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream(PROPERTY_FILE);
        try {
//            bufferedReader = new BufferedReader(systemResourceAsStream);
            properties.load(systemResourceAsStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Instances retrieveInstances(
            ResultSet rs) throws Exception {
        ResultSetMetaData md = rs.getMetaData();

        // Determine structure of the instances
        int numAttributes = md.getColumnCount();
        int[] attributeTypes = new int[numAttributes];
        @SuppressWarnings("unchecked")
        Hashtable<String, Double>[] nominalIndexes = new Hashtable[numAttributes];
        @SuppressWarnings("unchecked")
        ArrayList<String>[] nominalStrings = new ArrayList[numAttributes];
        for (int i = 1; i <= numAttributes; i++) {

            switch (translateDBColumnType(md.getColumnTypeName(i))) {

                case STRING:
                    // System.err.println("String --> nominal");
                    attributeTypes[i - 1] = Attribute.NOMINAL;
                    nominalIndexes[i - 1] = new Hashtable<String, Double>();
                    nominalStrings[i - 1] = new ArrayList<String>();
                    break;
                case TEXT:
                    // System.err.println("Text --> string");
                    attributeTypes[i - 1] = Attribute.STRING;
                    nominalIndexes[i - 1] = new Hashtable<String, Double>();
                    nominalStrings[i - 1] = new ArrayList<String>();
                    break;
                case BOOL:
                    // System.err.println("boolean --> nominal");
                    attributeTypes[i - 1] = Attribute.NOMINAL;
                    nominalIndexes[i - 1] = new Hashtable<String, Double>();
                    nominalIndexes[i - 1].put("false", new Double(0));
                    nominalIndexes[i - 1].put("true", new Double(1));
                    nominalStrings[i - 1] = new ArrayList<String>();
                    nominalStrings[i - 1].add("false");
                    nominalStrings[i - 1].add("true");
                    break;
                case DOUBLE:
                    // System.err.println("BigDecimal --> numeric");
                    attributeTypes[i - 1] = Attribute.NUMERIC;
                    break;
                case BYTE:
                    // System.err.println("byte --> numeric");
                    attributeTypes[i - 1] = Attribute.NUMERIC;
                    break;
                case SHORT:
                    // System.err.println("short --> numeric");
                    attributeTypes[i - 1] = Attribute.NUMERIC;
                    break;
                case INTEGER:
                    // System.err.println("int --> numeric");
                    attributeTypes[i - 1] = Attribute.NUMERIC;
                    break;
                case LONG:
                    // System.err.println("long --> numeric");
                    attributeTypes[i - 1] = Attribute.NUMERIC;
                    break;
                case FLOAT:
                    // System.err.println("float --> numeric");
                    attributeTypes[i - 1] = Attribute.NUMERIC;
                    break;
                case DATE:
                    attributeTypes[i - 1] = Attribute.DATE;
                    break;
                case TIME:
                    attributeTypes[i - 1] = Attribute.DATE;
                    break;
                case TIMESTAMP:
                    attributeTypes[i - 1] = Attribute.DATE;
                    break;
                default:
                    attributeTypes[i - 1] = Attribute.STRING;
            }
        }
        queryList = new ArrayList<>();
        //获得表字段名
        columnNames = new Vector<String>();
        for (int i = 0; i < numAttributes; i++) {
            String columnLabel = md.getColumnLabel(i + 1);
            columnNames.add(columnLabel);
        }


        while (rs.next()) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < numAttributes; i++) {
                String columnLabel = md.getColumnLabel(i + 1);
                String columnValue = rs.getObject(i + 1).toString();
                map.put(columnLabel, columnValue);
            }
            queryList.add(map);
        }


        ArrayList<Instance> instances = new ArrayList<Instance>();
        int rowCount = 0;
        while (rs.next()) {
            double[] vals = new double[numAttributes];
            for (int i = 1; i <= numAttributes; i++) {
                switch (translateDBColumnType(md.getColumnTypeName(i))) {
                    case STRING:
                        String str = rs.getString(i);

                        if (rs.wasNull()) {
                            vals[i - 1] = Utils.missingValue();
                        } else {
                            Double index = nominalIndexes[i - 1].get(str);
                            if (index == null) {
                                index = new Double(nominalStrings[i - 1].size());
                                nominalIndexes[i - 1].put(str, index);
                                nominalStrings[i - 1].add(str);
                            }
                            vals[i - 1] = index.doubleValue();
                        }
                        break;
                    case TEXT:
                        String txt = rs.getString(i);

                        if (rs.wasNull()) {
                            vals[i - 1] = Utils.missingValue();
                        } else {
                            Double index = nominalIndexes[i - 1].get(txt);
                            if (index == null) {
                                index = new Double(nominalStrings[i - 1].size()) + 1;
                                nominalIndexes[i - 1].put(txt, index);
                                nominalStrings[i - 1].add(txt);
                            }
                            vals[i - 1] = index.doubleValue();
                        }
                        break;
                    case BOOL:
                        boolean boo = rs.getBoolean(i);
                        if (rs.wasNull()) {
                            vals[i - 1] = Utils.missingValue();
                        } else {
                            vals[i - 1] = (boo ? 1.0 : 0.0);
                        }
                        break;
                    case DOUBLE:
                        // BigDecimal bd = rs.getBigDecimal(i, 4);
                        double dd = rs.getDouble(i);
                        // Use the column precision instead of 4?
                        if (rs.wasNull()) {
                            vals[i - 1] = Utils.missingValue();
                        } else {
                            // newInst.setValue(i - 1, bd.doubleValue());
                            vals[i - 1] = dd;
                        }
                        break;
                    case BYTE:
                        byte by = rs.getByte(i);
                        if (rs.wasNull()) {
                            vals[i - 1] = Utils.missingValue();
                        } else {
                            vals[i - 1] = by;
                        }
                        break;
                    case SHORT:
                        short sh = rs.getShort(i);
                        if (rs.wasNull()) {
                            vals[i - 1] = Utils.missingValue();
                        } else {
                            vals[i - 1] = sh;
                        }
                        break;
                    case INTEGER:
                        int in = rs.getInt(i);
                        if (rs.wasNull()) {
                            vals[i - 1] = Utils.missingValue();
                        } else {
                            vals[i - 1] = in;
                        }
                        break;
                    case LONG:
                        long lo = rs.getLong(i);
                        if (rs.wasNull()) {
                            vals[i - 1] = Utils.missingValue();
                        } else {
                            vals[i - 1] = lo;
                        }
                        break;
                    case FLOAT:
                        float fl = rs.getFloat(i);
                        if (rs.wasNull()) {
                            vals[i - 1] = Utils.missingValue();
                        } else {
                            vals[i - 1] = fl;
                        }
                        break;
                    case DATE:
                        Date date = rs.getDate(i);
                        if (rs.wasNull()) {
                            vals[i - 1] = Utils.missingValue();
                        } else {
                            vals[i - 1] = date.getTime();
                        }
                        break;
                    case TIME:
                        Time time = rs.getTime(i);
                        if (rs.wasNull()) {
                            vals[i - 1] = Utils.missingValue();
                        } else {
                            vals[i - 1] = time.getTime();
                        }
                        break;
                    case TIMESTAMP:
                        Timestamp ts = rs.getTimestamp(i);
                        if (rs.wasNull()) {
                            vals[i - 1] = Utils.missingValue();
                        } else {
                            vals[i - 1] = ts.getTime();
                        }
                        break;
                    default:
                        vals[i - 1] = Utils.missingValue();
                }
            }
            Instance newInst;
            if (sparseData) {
                newInst = new SparseInstance(1.0, vals);
            } else {
                newInst = new DenseInstance(1.0, vals);
            }
            instances.add(newInst);
            rowCount++;
        }
        ArrayList<Attribute> attribInfo = new ArrayList<Attribute>();
        for (int i = 0; i < numAttributes; i++) {
            /* Fix for databases that uppercase column names */
            // String attribName = attributeCaseFix(md.getColumnName(i + 1));
            String attribName = attributeCaseFix(columnNames.get(i));
            switch (attributeTypes[i]) {
                case Attribute.NOMINAL:
                    attribInfo.add(new Attribute(attribName, nominalStrings[i]));
                    break;
                case Attribute.NUMERIC:
                    attribInfo.add(new Attribute(attribName));
                    break;
                case Attribute.STRING:
                    Attribute att = new Attribute(attribName, (ArrayList<String>) null);
                    attribInfo.add(att);
                    for (int n = 0; n < nominalStrings[i].size(); n++) {
                        att.addStringValue(nominalStrings[i].get(n));
                    }
                    break;
                case Attribute.DATE:
                    attribInfo.add(new Attribute(attribName, (String) null));
                    break;
                default:
                    throw new Exception("Unknown attribute type");
            }
        }
        Instances result = new Instances("QueryResult", attribInfo,
                instances.size());
        for (int i = 0; i < instances.size(); i++) {
            result.add(instances.get(i));
        }

        return result;
    }


    public static String attributeCaseFix(String columnName) {
        if (m_checkForUpperCaseNames) {
            String ucname = columnName.toUpperCase();
            if (ucname.equals(EXP_TYPE_COL.toUpperCase())) {
                return EXP_TYPE_COL;
            } else if (ucname.equals(EXP_SETUP_COL.toUpperCase())) {
                return EXP_SETUP_COL;
            } else if (ucname.equals(EXP_RESULT_COL.toUpperCase())) {
                return EXP_RESULT_COL;
            } else {
                return columnName;
            }
        } else if (m_checkForLowerCaseNames) {
            String ucname = columnName.toLowerCase();
            if (ucname.equals(EXP_TYPE_COL.toLowerCase())) {
                return EXP_TYPE_COL;
            } else if (ucname.equals(EXP_SETUP_COL.toLowerCase())) {
                return EXP_SETUP_COL;
            } else if (ucname.equals(EXP_RESULT_COL.toLowerCase())) {
                return EXP_RESULT_COL;
            } else {
                return columnName;
            }
        } else {
            return columnName;
        }
    }


    /**
     * translates the column data type string to an integer value that indicates
     * which data type / get()-Method to use in order to retrieve values from the
     * database (see DatabaseUtils.Properties, InstanceQuery()). Blanks in the
     * type are replaced with underscores "_", since Java property names can't
     * contain blanks.
     *
     * @param type the column type as retrieved with
     *             java.sql.MetaData.getColumnTypeName(int)
     * @return an integer value that indicates which data type / get()-Method to
     * use in order to retrieve values from the
     */
    public static int translateDBColumnType(String type) throws Exception {
        try {

            String value = properties.getProperty(type);
            String typeUnderscore = type.replaceAll(" ", "_");
            if (value == null) {
                value = properties.getProperty(typeUnderscore);
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unknown data type: " + type + ". "
                    + "Add entry in " + PROPERTY_FILE + ".\n"
                    + "If the type contains blanks, either escape them with a backslash "
                    + "or use underscores instead of blanks.");
        }
    }

    public Instances changeInstances() {
        try {
            if (connection == null) {
                connectionDataBase();
            }
            PreparedStatement preparedStatement = connection.prepareStatement(getQuery(),
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            preparedStatement.execute();
            ResultSet rs = preparedStatement.getResultSet();
            return retrieveInstances(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 测试是否连接数据库
     *
     * @return
     */
    public boolean connectionDataBase() {
        try {
            connection = DriverManager.getConnection(getDatabaseURL(), getUsername(),
                    getPassword());
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
