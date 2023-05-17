package pojos;

import java.time.LocalDate;

public class Student {
    private String firstname, lastname;
    private LocalDate birthdate;
    private String classname;

    @Override
    public String toString() {
        return "Student{" +
                "firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", birthdate=" + birthdate +
                ", classname='" + classname + '\'' +
                '}';
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public Student(String firstname, String lastname, LocalDate birthdate, String classname) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthdate = birthdate;
        this.classname = classname;
    }
}
