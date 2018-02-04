package uk.co.glass_software.android.shared_preferences.demo.model;

import java.util.Date;

public class Person {
    
    private int age;
    private String firstName;
    private String name;
    private Date lastSeenDate;
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public Date getLastSeenDate() {
        return lastSeenDate;
    }
    
    public void setLastSeenDate(Date lastSeenDate) {
        this.lastSeenDate = lastSeenDate;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        Person person = (Person) o;
        
        if (age != person.age) {
            return false;
        }
        if (firstName != null ? !firstName.equals(person.firstName) : person.firstName != null) {
            return false;
        }
        if (name != null ? !name.equals(person.name) : person.name != null) {
            return false;
        }
        return lastSeenDate != null ? lastSeenDate.equals(person.lastSeenDate) : person.lastSeenDate == null;
    }
    
    @Override
    public int hashCode() {
        int result = age;
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (lastSeenDate != null ? lastSeenDate.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Person{");
        sb.append("age=").append(age);
        sb.append(", firstName='").append(firstName).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", lastSeenDate=").append(lastSeenDate);
        sb.append('}');
        return sb.toString();
    }
}
