package SignInOutData;

public class Person implements Comparable<Person>
{
	String firstName;
	String lastName;
	String studentID;
	double timeSpent;
	public int compareTo(Person person)
	{
		int order = lastName.compareToIgnoreCase(person.lastName);
		if(order == 0)
		{
			return firstName.compareToIgnoreCase(person.firstName);
		}
		return order;
	}
	public boolean equals(Person person)
	{
		return studentID.equals(person.studentID);
	}
}
