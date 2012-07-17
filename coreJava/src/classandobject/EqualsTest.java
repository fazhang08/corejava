package classandobject;

public class EqualsTest 
{
	public static void main(String[] args)
	{
		Employee alice1 = new Employee("Alice Adams",7000,1987,12,15);
		
		Employee alice2 = alice1;
		
		Employee alice3 = new Employee("Alice Adams",7000,1987,12,15);
		
		Employee bob = new Employee("Bob Brandson",50000,1989,10,1);
		
		
		
		System.out.println("alice1 == alice2: " + (alice1 == alice2));
		
		System.out.println("alice1 == alice3: " + (alice1 == alice3));
		
		System.out.println("alice1.equals(alice3): " + (alice1.equals(alice3)));
		
		System.out.println("alice1.equals(bob): " + (alice1.equals(bob)));
		
		
		Manager carl = new Manager("Carl Cracker",80000,1987,12,15);
		
		Manager boss = new Manager("Carl Cracker",80000,1987,12,15);
		
		boss.setBonus(5000);
		
		System.out.println("boss.toString(): " + boss);
		
		System.out.println("carl.equals(boss): " + carl.equals(boss));
		
		System.out.println("alice1.hashCode(): " + alice1.hashCode());
		
		System.out.println("alice3.hashCode(): " + alice3.hashCode());
		
		System.out.println("bob.hashCode(): " + bob.hashCode());
		
		System.out.println("carl.hashCode(): " + carl.hashCode());
		
		
	}
}

class Manager extends Employee
{
	public Manager(String n, double s, int year, int month, int day)
	{
		super(n,s,year,month,day);
		
		bonus=0;
	}
	
	public void setBonus(int i) {
		
		bonus = i;
		
	}

	public double getSalary()
	{
		double baseSalary =super.getSalary();
		
		return baseSalary + bonus;
	}
	
	public boolean equals(Object otherObject)
	{
		if(!super.equals(otherObject)) return false;
		
		Manager other = (Manager)otherObject;
		
		return bonus ==other.bonus;
		
	}
	
	public int hashCode()
	{
		return super.hashCode()+17*new Double(bonus).hashCode();
	}
	
	public String toString()
	{
		return super.toString()+" [ bonus= " + bonus + " ]";
	}
	private double bonus;
}