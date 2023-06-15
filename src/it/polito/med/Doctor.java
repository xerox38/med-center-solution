package it.polito.med;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

class Doctor {
	
	private final String id;
	private final String name;
	private final String surname;
	private final Map<String,Set<String>> slots = new HashMap<>();
	private int totalSlots;
	private int totalAppointments;

	public Doctor(String id, String name, String surname) {
		this.id = id;
		this.name= name;
		this.surname = surname;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSurname() {
		return surname;
	}

	public void addSlot(String date, String current, String next) {
		
		slots .computeIfAbsent(date, d -> new TreeSet<>()).add(current+"-"+next);
		
	}

	public boolean hasSlotsOn(String date) {
		return slots.containsKey(date);
	}

	public List<String> getSlotsOn(String date) {
		return new LinkedList<>(slots.get(date));
	}

	public boolean validate(String date, String slot) throws MedException {
		Set<String> ss = slots.get(date);
		if(ss==null) throw new MedException("Invalid date " + date);
		if(!ss.contains(slot)) throw new MedException("Invalid slot " + slot + "/ not in " + ss);
		this.totalAppointments++;
		return true;
	}

	public void incrementSlots(int count) {
		this.totalSlots += count;		
	}
	
	public double completeness() {
		if(MedManager.debug) System.out.println(id + " : " + totalAppointments + " / "+ totalSlots);
		return (double)totalAppointments / (double)totalSlots;
	}
	
}
