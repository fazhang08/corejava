package intinner;

import java.util.*;

public class CloneTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try
		{
			Employee1 original = new Employee1("John Q.Public", 50000);
			
			original.setHireDay(2000, 1, 1);
			
			Employee1 copy = original.clone();
			
			copy.raiseSalary(10);
			
			copy.setHireDay(2002, 12, 31);
			
			System.out.println("original= " + original);
			
			System.out.println("copy= " + copy);
			
		}catch(CloneNotSupportedException e)
		{
			e.printStackTrace();
		}

	}

}

 class Employee1 implements Cloneable
{
	public Employee1(String n, double s)
	{
		name = n;
		
		salary = s;
		
		hireDay = new Date();
	}
	
	public Employee1 clone()throws CloneNotSupportedException
	{
		Employee1 cloned = (Employee1)super.clone();
		
		cloned.hireDay = (Date) hireDay.clone();
		
		return cloned;
	}
	
	public void setHireDay(int year, int month, int day)
	{
		Date newHireDay = new GregorianCalendar(year,month,day).getTime();
		
		hireDay.setTime(newHireDay.getTime());
	}
	
	public void raiseSalary(double byPercent)
	{
		double rasie = salary * byPercent/100;
		
		salary += rasie;
	}
	
	public String toString()
	{
		return "Employee1[name= " + name + " , salary= " + salary + ", hireDay= " + hireDay + "]";
	}
	
	private String name;
	
	private double salary;
	
	private Date hireDay;
}
