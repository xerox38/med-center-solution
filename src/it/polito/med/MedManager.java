package it.polito.med;
/*kawsar seii */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class MedManager {

	public static boolean debug = false;
	
	private final Map<String,List<String>> specs = new HashMap<>();
	private final Map<String,Doctor> docs = new HashMap<>();
	private int appCounter;
	private final Map<String, Appointment> appointments = new HashMap<>();
	private String currentDate;
	private final Map<String, SortedSet<Appointment>> accepted = new HashMap<>();

	/**
	 * add a set of medical specialities to the list of specialities
	 * offered by the med centre.
	 * Method can be invoked multiple times.
	 * Possible duplicates are ignored.
	 * 
	 * @param specialities the specialities
	 */
	public void addSpecialities(String... specialities) {
		for( String spec : specialities) {
			this.specs.put(spec,new ArrayList<>());
		}
		
	}

	/**
	 * retrieves the list of specialities offered in the med centre
	 * 
	 * @return list of specialities
	 */
	public Collection<String> getSpecialities() {
		return specs.keySet();
	}
	
	
	/**
	 * adds a new doctor with the list of their specialities
	 * 
	 * @param id		unique id of doctor
	 * @param name		name of doctor
	 * @param surname	surname of doctor
	 * @param speciality speciality of the doctor
	 * @throws MedException in case of duplicate id or non-existing speciality
	 */
	public void addDoctor(String id, String name, String surname, String speciality) throws MedException {
		if( docs.containsKey(id) ) {
			throw new MedException("Duplicate doctor id: " + id);
		}
		if( !specs.containsKey(speciality) ) {
			throw new MedException("Invalid speciality: " + speciality);
		}
		Doctor d = new Doctor(id,name,surname);
		docs.put(id,d);
		specs.get(speciality).add(id);
	}

	/**
	 * retrieves the list of doctors with the given speciality
	 * 
	 * @param speciality required speciality
	 * @return the list of doctor ids
	 */
	public Collection<String> getSpecialists(String speciality) {
		return Collections.unmodifiableCollection(specs.get(speciality));
	}

	/**
	 * retrieves the name of the doctor with the given code
	 * 
	 * @param code code id of the doctor 
	 * @return the name
	 */
	public String getDocName(String code) {
		return docs.get(code).getName();
	}

	/**
	 * retrieves the surname of the doctor with the given code
	 * 
	 * @param code code id of the doctor 
	 * @return the surname
	 */
	public String getDocSurname(String code) {
		return docs.get(code).getSurname();
	}

	/**
	 * Define a schedule for a doctor on a given day.
	 * Slots are created between start and end hours with a 
	 * duration expressed in minutes.
	 * 
	 * @param code	doctor id code
	 * @param date	date of schedule
	 * @param start	start time
	 * @param end	end time
	 * @param duration duration in minutes
	 * @return the number of slots defined
	 */
	public int addDailySchedule(String code, String date, String start, String end, int duration) {
		Doctor doc = docs.get(code);
		String[] startParts = start.split(":");
		int h = Integer.parseInt(startParts[0]);
		int m = Integer.parseInt(startParts[1]);
		String current = start;
		int count = 0;
		int total = m;
		while( true ) {
			
			
			total += duration;
			int hh = h + total/60;
			int mm = total % 60;
			String next = String.format("%02d:%02d",hh,mm);
			
			if( next.compareTo(end) > 0 ) break;
			doc.addSlot(date,current,next);
			current = next;
			count++;
		}
		doc.incrementSlots(count);
		
		return count;
	}

	/**
	 * retrieves the available slots available on a given date for a speciality.
	 * The returned map contains an entry for each doctor that has slots scheduled on the date.
	 * The map contains a list of slots described as strings with the format "hh:mm-hh:mm",
	 * e.g. "14:00-14:30" describes a slot starting at 14:00 and lasting 30 minutes.
	 * 
	 * @param date			date to look for
	 * @param speciality	required speciality
	 * @return a map doc-id -> list of slots in the schedule
	 */
	public Map<String, List<String>> findSlots(String date, String speciality) {
		
		return specs.get(speciality).stream()
				.map(docs::get)
				.filter(d -> d.hasSlotsOn(date))
				.collect(Collectors.toMap(Doctor::getId,
										  d -> d.getSlotsOn(date)))
				;
	}

	/**
	 * Define an appointment for a patient in an existing slot of a doctor's schedule
	 * 
	 * @param ssn		ssn of the patient
	 * @param name		name of the patient
	 * @param surname	surname of the patient
	 * @param code		code id of the doctor
	 * @param date		date of the appointment
	 * @param slot		slot to be booked
	 * @return a unique id for the appointment
	 * @throws MedException	in case of invalid code, date or slot
	 */
	public String setAppointment(String ssn, String name, String surname, String code, String date, String slot) throws MedException {
		Doctor doc = docs.get(code);
		if(doc==null) {
			throw new MedException("Invalid doctor id: " + code);
		}

		doc.validate(date,slot);

		String appId = "APP"+(++appCounter);
		Appointment app = new Appointment(appId, ssn,name,surname,date,slot,doc);
		
		appointments.put(appId, app);
		
		return appId;
	}

	/**
	 * retrieves the doctor for an appointment
	 * 
	 * @param idAppointment id of appointment
	 * @return doctor code id
	 */
	public String getAppointmentDoctor(String idAppointment) {
		
		return appointments.get(idAppointment).getDoctor();
	}

	/**
	 * retrieves the patient for an appointment
	 * 
	 * @param idAppointment id of appointment
	 * @return doctor patient ssn
	 */
	public String getAppointmentPatient(String idAppointment) {
		return appointments.get(idAppointment).getSsn();
	}

	/**
	 * retrieves the time for an appointment
	 * 
	 * @param idAppointment id of appointment
	 * @return time of appointment
	 */
	public String getAppointmentTime(String idAppointment) {
		return appointments.get(idAppointment).getTime();
	}

	/**
	 * retrieves the date for an appointment
	 * 
	 * @param idAppointment id of appointment
	 * @return date
	 */
	public String getAppointmentDate(String idAppointment) {
		return appointments.get(idAppointment).getDate();
	}

	/**
	 * retrieves the list of a doctor appointments for a given day.
	 * Appointments are reported as string with the format
	 * "hh:mm=SSN"
	 * 
	 * @param code doctor id
	 * @param date date required
	 * @return list of appointments
	 */
	public Collection<String> listAppointments(String code, String date) {
		
		return appointments.values().stream()
				.filter(a -> a.getDoctor().equals(code) && a.getDate().equals(date))
				.map(Appointment::toString)
				.toList()
				;
	}

	/**
	 * Define the current date for the medical centre
	 * The date will be used to accept patients arriving at the centre.
	 * 
	 * @param date	current date
	 * @return the number of total appointments for the day
	 */
	public int setCurrentDate(String date) {
		currentDate = date;
		return (int) appointments.values().stream().filter(a->a.getDate().equals(date)).count();
	}

	/**
	 * mark the patient as accepted by the med centre reception
	 * 
	 * @param ssn SSN of the patient
	 */
	public void accept(String ssn) {
		appointments.values().stream()
			.filter(a -> a.getDate().equals(currentDate) && a.getSsn().equals(ssn))
			.forEach(a -> {
				accepted .computeIfAbsent(a.getDoctor(), d->new TreeSet<>()).add(a);
				a.setAccepted();
			});
	}

	/**
	 * returns the next appointment of a patient that has been accepted.
	 * Returns the id of the earliest appointment whose patient has been
	 * accepted and the appointment not completed yet.
	 * Returns null if no such appointment is available.
	 * 
	 * @param code	code id of the doctor
	 * @return appointment id
	 */
	public String nextAppointment(String code) {
		SortedSet<Appointment> apps = accepted.get(code);
		if(apps==null || apps.size()==0){
			return null;
		}
		return apps.first().getId();
	}

	/**
	 * mark an appointment as complete.
	 * The appointment must be with the doctor with the given code
	 * the patient must have been accepted
	 * 
	 * @param code		doctor code id
	 * @param appId		appointment id
	 * @throws MedException in case code or appointment code not valid,
	 * 						or appointment with another doctor
	 * 						or patient not accepted
	 * 						or appointment not for the current day
	 */
	public void completeAppointment(String code, String appId)  throws MedException {
		Doctor doc = docs.get(code);
		if(doc==null) {
			throw new MedException("Invalid doctor id: " + code);
		}

		Appointment app = this.appointments.get(appId);
		if(app==null) {
			throw new MedException("Invalid app id: " + appId);
		}
		
		SortedSet<Appointment> apps = accepted.get(code);
		if( apps==null || ! apps.contains(app) ) {
			throw new MedException("Patient not accepted for app id: " + appId);
		}
		
		apps.remove(app);
	}

	/**
	 * computes the show rate for the appointments of a doctor on a given date.
	 * The rate is the ratio of accepted patients over the number of appointments
	 *  
	 * @param code		doctor id
	 * @param date		reference date
	 * @return	no show rate
	 */
	public double showRate(String code, String date) {
		class Counters {
			int accepted;
			int total;
			void process(Appointment a) {
				total++;
				if(a.isAccepted()) accepted++;
			}
			double ratio() { return (double)accepted/(double)total; }
		}
		Counters counters = new Counters();
		appointments.values().stream()
			.filter(a-> a.getDoctor().equals(code) && a.getDate().equals(date))
			.forEach(counters::process);
		return counters.ratio();
	}

	/**
	 * computes the schedule completeness for all doctors of the med centre.
	 * The completeness for a doctor is the ratio of the number of appointments
	 * over the number of slots in the schedule.
	 * The result is a map that associates to each doctor id the relative completeness
	 * 
	 * @return the map id : completeness
	 */
	public Map<String, Double> scheduleCompleteness() {
		
		return docs.values().stream()
				.collect(Collectors.toMap(Doctor::getId, Doctor::completeness))
				;
	}


	
}
