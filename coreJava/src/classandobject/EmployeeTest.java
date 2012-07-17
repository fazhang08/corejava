package classandobject;
import java.util.*;
public class EmployeeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		Employee[] staff = new Employee[3];
		
		staff[0] = new Employee("Carl Cracker",75000,1987,12,15);
		staff[1] = new Employee("Hary Hacker",50000,1989,10,1);
		staff[2] = new Employee("Tony Tester",40000,1990,3,15);

		System.out.println("Original employee info :");
		for(Employee emp: staff)
		{
			System.out.println("name : "+emp.getName()+" , salary : "+emp.getSalary()+ " , hireDay : "+emp.getHireDay());
		}
		//raise everyone's salay by 5%
		
		for(Employee emp: staff)
		{
			emp.raiseSalary(5);
		}
		

		System.out.println("After raise salary employee info :");
		for(Employee emp: staff)
		{
			System.out.println("name : "+emp.getName()+" , salary : "+emp.getSalary()+ " , hireDay : "+emp.getHireDay());
		}
	}
	
	
}

class Employee
{
	public Employee(String n, double s, int year, int month, int day)
	{
		this.name = n;
		
		this.salary = s;
		
		GregorianCalendar calendar = new GregorianCalendar(year,month-1,day);
		
		this.hireDay = calendar.getTime();
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getSalary() {
		return salary;
	}

	public void setSalary(double salary) {
		this.salary = salary;
	}

	public Date getHireDay() {
		return hireDay;
	}

	public void setHireDay(Date hireDay) {
		this.hireDay = hireDay;
	}

	public void raiseSalary(double byPercent)
	{
		double raise = this.salary*byPercent/100;
		
		this.salary += raise;
	}
	
	@Override
	public boolean equals(Object otherObject)
	{
		if(this == otherObject) return true;
		
		if(otherObject == null) return false;
		
		if(getClass() !=otherObject.getClass()) return false;
		
		Employee other = (Employee)otherObject;
		
		return name.equals(other.name)&&salary == other.salary&&hireDay.equals(other.hireDay);

	}
	
	public int hashCode()
	{
		return 7*name.hashCode()+11*new Double(salary).hashCode()+13*hireDay.hashCode();
	}
	
	public String toString()
	{
		return getClass().getName()+" [name= "+name+" , salary= "+salary+" ,hireDay= "+hireDay;
	}
	private String name ;
	
	private double salary;
	
	private Date hireDay;
}