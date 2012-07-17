package classandobject;

import java.lang.reflect.*;
import java.util.*;

public class ReflectionTest {

    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        
    	String name = " ";
    	
    	if(args.length > 0)
    	{
    		name = args[0];
    	}else
    	{
    		Scanner in = new Scanner(System.in);
    		
    		System.out.println("Enter class name (e.g. java.util.Date): ");
    		
    		name = in.next();
    	}
    	
    	try
    	{
    		Class cl = Class.forName(name);
    		
    		Class suppercl = cl.getSuperclass();
    		
    		String modifiers = Modifier.toString(cl.getModifiers());
    		
    		if(modifiers.length()>0)
    			
    			System.out.print(modifiers + " ");
    		
    		System.out.print("class " + name);
    		
    		if(suppercl != null &&suppercl != Object.class)
    			
    			System.out.print(" extends " + suppercl.getName());
    		
    		System.out.print("\n{\n");
    		
    		printConstructors(cl);
    		
    		System.out.println();
    		
    		printMethods(cl);
    		
    		System.out.println();
    		
    		printFields(cl);
    		
    		System.out.println("}");
    			
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}

    }
    
    public static void printConstructors(Class cl)
    {
        
        try
        {
            Constructor[] constructor = cl.getConstructors();
            
            for(Constructor c : constructor)
            {
                String name = c.getName();
                
                System.out.print(" ");
                
                String modifiers = Modifier.toString(c.getModifiers());
                
                if(modifiers.length()>0)
                    
                    System.out.print(modifiers + " ");
                
                System.out.print(name + "(");
                
                Class[] paramTypes = c.getParameterTypes();
                
                for(int j=0; j<paramTypes.length; j++)
                {
                    if(j>0)
                        
                        System.out.print(", ");
                    
                    System.out.print(paramTypes[j].getName());
                    
                }
                
                System.out.println(");");
                
            }
            
        }catch(Exception e)
        {
            System.out.println("printConstructors method exist error : ");
            
            e.printStackTrace();
        }
    }
    
    public static void printMethods(Class cl)
    {
    	try
    	{
    		Method[] methods = cl.getDeclaredMethods();
    		
    		for(Method m : methods)
    		{
    			Class retType = m.getReturnType();
    			
    			String name = m.getName();
    			
    			System.out.print(" ");
    			
    			String modifiers = Modifier.toString(m.getModifiers());
    			
    			if(modifiers.length()>0)
    				
    				System.out.print(modifiers + " ");
    				
    			System.out.print(retType.getName() + " " + name + "(");
    			
    			Class[] paramTypes = m.getParameterTypes();
    			
    			for(int j = 0; j<paramTypes.length; j++)
    			{
    				if(j>0)
    					
    					System.out.print(", ");
    				
    				System.out.print(paramTypes[j].getName());
    			}
    			
    			System.out.println(");");
    		}
    		
    	}catch(Exception e)
    	{
    		System.out.println("printMethods method exist error : ");
            
            e.printStackTrace();
    	}
    }

    
    public static void printFields(Class cl)
    {
    	try
    	{
    		Field[] fields = cl.getDeclaredFields();
    		
    		for(Field f : fields)
    		{
    			Class type = f.getType();
    			
    			String name = f.getName();
    			
    			System.out.print(" ");
    			
    			String modifiers = Modifier.toString(f.getModifiers());
    			
    			if(modifiers.length()>0)
    				
    				System.out.print(modifiers + " ");
    			
    			System.out.println(type.getName() + " " +name + ";");
    				
    		}
    	}catch(Exception e)
    	{

    		System.out.println("printFields method exist error : ");
            
            e.printStackTrace();
    	}
    }
}
