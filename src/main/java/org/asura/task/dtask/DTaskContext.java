package org.asura.task.dtask;

import java.io.Serializable;

import org.asura.core.data.DataRecord;

public class DTaskContext implements Serializable {

	private static final long serialVersionUID = -6333822137527222922L;

	private DataRecord data = new DataRecord();

	public String[] getAllFields() {
		return data.getAllFields();
	}

	public String getFieldValue(String field) {
		return data.getFieldValue(field);
	}

	public Object getFieldObject(String field) {
		return data.getFieldObject(field);
	}

	public void deleteField(String field) {
		data.deleteField(field);
	}

	public boolean containsField(String field) {
		return data.containsField(field);
	}

	public void AddField(String field, String value) {
		data.AddField(field, value);
	}

	public void AddField(String field, Object value) {
		data.AddField(field, value);
	}

}
