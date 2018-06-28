package chatte.history;

import java.util.Set;

public interface HistoryLogger {

	void recordMessage(String message, Set<String> resources);
	
}
