import pojos.Student;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DB_Access {

    private DB_Database database = DB_Database.getDBDatabase();
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final String INSERT_STUDENTS = "INSERT INTO student (catno, classid, release_date, price) VALUES (?, ?, ?, ?)";
    private PreparedStatement INSERT_STUDENT_STATEMENT;

    public void loadStudents() throws IOException, SQLException {
        /*String list = Files.readString(Path.of("C:\\Users\\Rapha\\Java\\StudentDatabasePostgreSQL\\src\\main\\resources\\Students_3xHIF_2023.csv"));
        System.out.println(list);*/
        List<Student> students = new BufferedReader(new FileReader("C:\\Users\\Rapha\\Java\\StudentDatabasePostgreSQL\\src\\main\\resources\\Students_3xHIF_2023.csv")).lines()
                .skip(1)
                .map(c -> {
                    String[] props = c.split(";");
                    return new Student(props[2], props[1], LocalDate.parse(props[3], dtf), props[0]);
                })
                .sorted((c, d) -> c.getLastname().compareTo(d.getLastname()))
                .collect(Collectors.toList());
        Map<String, List<Student>> classStudentMap = new HashMap<>();
        for (Student student : students) {
            if (classStudentMap.containsKey(student.getClassname())) {
                classStudentMap.get(student.getClassname()).add(student);
            }
            else {
                List<Student> stud = new ArrayList<>();
                stud.add(student);
                classStudentMap.put(student.getClassname(), stud);
            }
        }
        //System.out.println(classStudentMap.get("3DHIF"));
        addClassesToDatabase(classStudentMap);
    }

    private final String INSERT_CLASS_DATA_STRING = "INSERT INTO grade (classname) VALUES (?)";
    private PreparedStatement INSERT_CLASS_DATE_STATEMENT;
    private final String CLEAR_TABLE_VALUES_STUDENT = "DELETE FROM student";
    private final String CLEAR_TABLE_VALUES_GRADE = "DELETE FROM grade";

    private void addClassesToDatabase(Map<String, List<Student>> classStudentMap) throws SQLException {

        Statement statement = database.getStatement();
        statement.executeUpdate(CLEAR_TABLE_VALUES_STUDENT);
        statement.executeUpdate(CLEAR_TABLE_VALUES_GRADE);
        statement.close();

        if (INSERT_CLASS_DATE_STATEMENT == null) {
            INSERT_CLASS_DATE_STATEMENT = database.getConnection().prepareStatement(INSERT_CLASS_DATA_STRING);
        }

        for (String string : classStudentMap.keySet()) {
            INSERT_CLASS_DATE_STATEMENT.setString(1, string);
            INSERT_CLASS_DATE_STATEMENT.executeLargeUpdate();
        }
        addStudentsToDatabase(classStudentMap);
    }

    private final String INSERT_STUDENT_DATA_STRING = "INSERT INTO student (catno, classid, firstname, lastname, dateofbirth) VALUES (?, ?, ?, ?, ?)";
    private final String GET_CLASS_DATA = "SELECT * FROM grade";
    private PreparedStatement INSERT_STUDENT_DATA_STATEMENT;

    private void addStudentsToDatabase(Map<String, List<Student>> classStudentMap) throws SQLException {
        Map<String, Integer> classNameID = new HashMap<>();

        Statement statement = database.getStatement();
        ResultSet rs = statement.executeQuery(GET_CLASS_DATA);

        while (rs.next()) {
            classNameID.put(rs.getString("classname"), rs.getInt("classid"));
        }

        if (INSERT_STUDENT_DATA_STATEMENT == null) {
            INSERT_STUDENT_DATA_STATEMENT = database.getConnection().prepareStatement(INSERT_STUDENT_DATA_STRING);
        }

        for (String classname : classStudentMap.keySet()) {
            List<Student> studentList = classStudentMap.get(classname);
            for (int i = 0; i<studentList.size(); i++) { //catno, classid, firstname, lastname, dateofbirth
                INSERT_STUDENT_DATA_STATEMENT.setInt(1, i+1);
                INSERT_STUDENT_DATA_STATEMENT.setInt(2, classNameID.get(classname));
                INSERT_STUDENT_DATA_STATEMENT.setString(3, studentList.get(i).getFirstname());
                INSERT_STUDENT_DATA_STATEMENT.setString(4, studentList.get(i).getLastname());
                INSERT_STUDENT_DATA_STATEMENT.setDate(5, java.sql.Date.valueOf(studentList.get(i).getBirthdate()));
                INSERT_STUDENT_DATA_STATEMENT.executeLargeUpdate();
            }
        }

    }

    public List<String> getAllClassNames() throws SQLException {
        Statement statement = database.getStatement();
        ResultSet rs = statement.executeQuery("SELECT classname FROM grade");
        List<String> classNameList = new ArrayList<>();
        while (rs.next()) {
            System.out.println(rs.getString("classname"));
            classNameList.add(rs.getString("classname"));
        }
        return classNameList;
    }

    public List<Student> getStudents(String classname) throws SQLException {
        Statement statement = database.getStatement();

        Map<Integer, String> classNameID = new HashMap<>();
        ResultSet rsC = statement.executeQuery(GET_CLASS_DATA);

        while (rsC.next()) {
            classNameID.put(rsC.getInt("classid"), rsC.getString("classname"));
        }

        ResultSet rs = statement.executeQuery("SELECT * FROM student" + ((classname.equals("")) ? "" : " INNER JOIN grade ON grade.classid = student.classid  WHERE classname = " + "'" + classname + "'"));
        List<Student> studentList = new ArrayList<>();
        while (rs.next()) {
            Student student = new Student(rs.getString("firstname"), rs.getString("lastname"), LocalDate.parse(rs.getDate("dateofbirth").toString()), classNameID.get(rs.getInt("classid")));
            studentList.add(student);
        }
        return studentList;

    }

    private final String ADD_STUDENT_TO_DB = "INSERT INTO student (catno, classid, firstname, lastname, dateofbirth) VALUES (?, ?, ?, ?, ?)";
    private PreparedStatement ADD_STUDENT;

    public void addStudent(Student student) throws SQLException {

        if (ADD_STUDENT == null) {
            ADD_STUDENT = database.getConnection().prepareStatement(ADD_STUDENT_TO_DB);
        }
        Statement statement = database.getStatement();
        int id = 0;

        ResultSet rs = statement.executeQuery(("SELECT classid FROM grade WHERE classname = " + "'" + student.getClassname() + "'"));
        while (rs.next()) {
            id = rs.getInt("classid");
        }
        rs.next();
        List<Student> classList = new ArrayList<>();
        classList.add(student);
        ResultSet crs = database.getStatement().executeQuery("SELECT * FROM student WHERE classid = " + id);

        while (crs.next()) {
            classList.add(new Student(crs.getString("firstname"), crs.getString("lastname"), LocalDate.parse(crs.getDate("dateofbirth").toString()), student.getClassname()));
        }

        classList = classList.stream().sorted((c, d) -> c.getLastname().compareTo(d.getLastname())).collect(Collectors.toList());

        for (int i = 0; i < classList.size(); i++) {

            if (INSERT_STUDENT_DATA_STATEMENT == null) {
                INSERT_STUDENT_DATA_STATEMENT = database.getConnection().prepareStatement(INSERT_STUDENT_DATA_STRING);
            }

            INSERT_STUDENT_DATA_STATEMENT.setInt(1, i+1);
            INSERT_STUDENT_DATA_STATEMENT.setInt(2, id);
            INSERT_STUDENT_DATA_STATEMENT.setString(3, classList.get(i).getFirstname());
            INSERT_STUDENT_DATA_STATEMENT.setString(4, classList.get(i).getLastname());
            INSERT_STUDENT_DATA_STATEMENT.setDate(5, java.sql.Date.valueOf(classList.get(i).getBirthdate()));
            INSERT_STUDENT_DATA_STATEMENT.executeLargeUpdate();
        }
    }

    public void exportDBToFile() throws IOException, SQLException {
        Map<Integer, String> classIdListMap = new HashMap<>();
        ResultSet rs = database.getStatement().executeQuery("SELECT * FROM grade");

        while (rs.next()) {
            classIdListMap.put(rs.getInt("classid"), rs.getString("classname"));
        }
        rs.next();


        String text = "firstname,lastname,catno,birthdate,classid\n";

        ResultSet crs = database.getStatement().executeQuery("SELECT * FROM student");
        while (crs.next()) {
           text += crs.getString("firstname") + "," + crs.getString("lastname") + "," + crs.getInt("catno") + "," + LocalDate.parse(crs.getDate("dateofbirth").toString()) + "," + classIdListMap.get(crs.getInt("classid")) + "\n";
        }

        // Defining the file name of the file
        Path fileName = Path.of(
                "C:\\Users\\Rapha\\OneDrive\\Documents\\Klei\\OxygenNotIncluded\\save_files\\Hovel\\studentdata.csv");

        // Writing into the file
        Files.writeString(fileName, text);

        // Reading the content of the file
        String file_content = Files.readString(fileName);

        // Printing the content inside the file
        System.out.println(file_content);
    }



    public static void main(String[] args) throws IOException, SQLException {
        Scanner sc = new Scanner(System.in);
            /*DB_Access db_access = new DB_Access();
            db_access.loadStudents();

        System.out.println("Students of 3DHIF");
            db_access.getStudents("3DHIF");
        System.out.println("All students");
            db_access.getStudents("");
            db_access.getAllClassNames();
        //System.out.println("Add student Cornelius");
        //    db_access.addStudent(new Student("Cornelius", "Vanderbilt", LocalDate.of(2005, 2, 2), "3DHIF"));
        db_access.exportDBToFile();

        JFrame frame = new JFrame();
        JButton btShowStudents = new JButton("Show all students");
        btShowStudents.setBounds(100, 200, 50, 20);
        frame.add(btShowStudents);

        frame.setVisible(true);
        */
        DB_Access dbAccess = new DB_Access();
        while (!false) {
            System.out.println("Welcome to the Student DB, what do you want to do\n" +
                    "1: Insert the Student List into the DB\n" +
                    "2: Get all classnames from the DB\n" +
                    "3: Get the Students\n" +
                    "4: Insert new Student into a class\n" +
                    "5: Export DB to a file\n" +
                    "6: Exit the program");

            try {
                int input = Integer.parseInt(sc.next());
                if (input < 1 || input > 6) throw new Exception();

                switch (input) {
                    case 1:
                        try {
                            dbAccess.loadStudents();
                        } catch (Exception e) {
                            System.out.println("An error occurred while adding students!");
                        }
                        break;
                    case 2:
                        try {
                            List<String> classnames = dbAccess.getAllClassNames();
                            classnames.forEach(System.out::println);
                        } catch (Exception e) {
                            System.out.println("An error occurred while getting classnames!");
                        }
                        break;
                    case 3:
                        try {
                            List<String> classnames = dbAccess.getAllClassNames();
                            System.out.println("Select a class:\n" +
                                    "1: " + classnames.get(0) +
                                    "\n2: " + classnames.get(1) +
                                    "\n3: " + classnames.get(2) +
                                    "\n4: " + classnames.get(3) +
                                    "\n5: " + classnames.get(4) +
                                    "\n6: " + classnames.get(5) +
                                    "\n7: get all students");
                            int selectedClass = 0;

                            try {
                                List<Student> studentList = new ArrayList<>();
                                selectedClass = Integer.parseInt(sc.next());
                                if (selectedClass <= 0 || selectedClass > 7) throw new Exception();
                                else if (selectedClass == 7) studentList = dbAccess.getStudents("");
                                else studentList = dbAccess.getStudents(classnames.get(selectedClass-1));
                                studentList.forEach(System.out::println);

                            } catch (Exception e) {
                                System.out.println("Not a valid number");
                            }

                        } catch (Exception e) {
                            System.out.println("An error occurred while getting students!");
                        }
                        break;
                    case 4:
                        String firstname = "";
                        String lastname = "";
                        LocalDate birthdate = LocalDate.of(1970, 1, 1);

                        try {
                            System.out.println("Firstname of student: ");
                            firstname = sc.next();
                            System.out.println("Lastname of student: ");
                            lastname = sc.next();
                            System.out.println("Birthdate of student (yyyy.MM.dd): ");
                            birthdate = LocalDate.parse(sc.next(), DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                            List<String> classnames = dbAccess.getAllClassNames();
                            System.out.println("Select a class:\n" +
                                    "1: " + classnames.get(0) +
                                    "\n2: " + classnames.get(1) +
                                    "\n3: " + classnames.get(2) +
                                    "\n4: " + classnames.get(3) +
                                    "\n5: " + classnames.get(4) +
                                    "\n6: " + classnames.get(5));
                            int selectedClass = 0;
                            try {
                                selectedClass = Integer.parseInt(sc.next());
                                if (selectedClass < 1 || selectedClass > 6) throw new Exception();

                                Student student = new Student(firstname, lastname, birthdate, classnames.get(selectedClass-1));
                                dbAccess.addStudent(student);
                            } catch (Exception e) {
                                System.out.println("Not a valid number");
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            System.out.println("An error occurred while reading data");
                        }
                        break;
                    case 5:
                        try {
                            dbAccess.exportDBToFile();
                        } catch (Exception e) {
                            System.out.println("An error occurred while reading data into file");
                        }
                        break;
                    case 6:
                        return;

                }

            } catch (Exception e) {
                System.out.println("Not a valid number");
            }

        }
        }
    }
