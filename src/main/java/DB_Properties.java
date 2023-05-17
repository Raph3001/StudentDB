import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DB_Properties {
    private static Properties PROPS = new Properties();

    static {
        InputStream is = DB_Properties.class.getResourceAsStream("database.properties");
        try {
            PROPS.load(is);
            System.out.println(PROPS.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getProperty(String value) {
        return PROPS.getProperty(value);
    }

    public static void main(String[] args) {
        System.out.println("L");
    }

}
