package intinner;

import java.util.Arrays;

public class EmployeeSortTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Employee[] staff = new Employee[3];
		
		staff[0] = new Employee("Harry Hacker", 35000);
		
		staff[1] = new Employee("Carl Cracker", 75000);
		
		staff[2] = new Employee("Tony Tester", 38000);
		
		Arrays.sort(staff);
		
		for(Employee e : staff)
			
			System.out.println("name=" + e.getName() + ", salary=" + e.getSalary());
		
		staff.clone();
			
	}

}
	 class Employee implements Comparable<Employee>
	{
		public Employee(String n, double s)
		{
			name = n;
			
			salary = s;
		}
		
		public String getName() {
			return name;
		}

		public double getSalary() {
			return salary;
		}
	    
		public void raiseSalary(double byPercent)
		{
			double raise = salary * byPercent/100;
			
			salary += raise;
		}
		@Override
		public int compareTo(Employee o) {

			if(salary < o.salary)
				return -1;
			else
				return 1;		
		}
		
		private String name ;
		
		private double salary;
	}
