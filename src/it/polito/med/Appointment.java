package it.polito.med;

class Appointment implements Comparable<Appointment>{
	final String id;
	final String ssn;
	final String name;
	final String surname;
	final String date;
	final String time;
	final Doctor doc;
	private boolean accepted;

	public Appointment(String id,String ssn, String name, String surname, String date, String slot, Doctor doc) {
		this.id = id;
		this.ssn = ssn;
		this.name = name;
		this.surname = surname;
		this.date = date;
		this.time = slot.replaceAll("-.+","");
		this.doc = doc;
	}

	public String getDoctor() {
		return doc.getId();
	}

	public String getSsn() {
		return ssn;
	}

	public String getTime() {
		return time;
	}

	public String getDate() {
		return date;
	}
	
	public String toString() {
		return time + "=" + ssn;
	}

	@Override
	public int compareTo(Appointment o) {

		return this.time.compareTo(o.time);
	}

	public String getId() {
		return id;
	}

	public void setAccepted() {
		this.accepted=true;
	}

	public boolean isAccepted() {
		return accepted;
	}
}
