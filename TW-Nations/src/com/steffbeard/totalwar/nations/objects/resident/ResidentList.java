package com.steffbeard.totalwar.nations.objects.resident;

import java.util.List;

public interface ResidentList {

	List<Resident> getResidents();

	boolean hasResident(String name);

	List<Resident> getOutlaws();
}
