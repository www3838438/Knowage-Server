/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.utilities.database;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractDataBase implements IDataBase {

	IDataSource dataSource;
	int varcharLength = 255;
	public static final String STANDARD_ALIAS_DELIMITER = "\"";

	private static transient Logger logger = Logger.getLogger(AbstractDataBase.class);

	public AbstractDataBase(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public int getVarcharLength() {
		return varcharLength;
	}

	@Override
	public void setVarcharLength(int varcharLength) {
		this.varcharLength = varcharLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.utilities.database.IDataBase#getUsedMemory(java.lang.String, java.lang.String)
	 */
	@Override
	public BigDecimal getUsedMemorySize(String schema, String tableNamePrefix) {
		logger.trace("IN");
		try {
			String query = getUsedMemorySizeQuery(schema, tableNamePrefix);
			if (query == null) {
				throw new DataBaseException("Impossible to build the query to get used memory size for the target database");
			}
			IDataStore dataStore = dataSource.executeStatement(query, 0, 0);
			if (dataStore.getRecordsCount() == 0) {
				throw new DataBaseException("The execution of the query used to get used memory size returned no result [" + query + "]");
			}

			BigDecimal size = null;
			IRecord record = dataStore.getRecordAt(0);
			for (int i = 0, l = record.getFields().size(); i < l; i++) {
				IField field = record.getFieldAt(i);
				if (field.getValue() instanceof Long) {
					size = BigDecimal.valueOf((Long) field.getValue());
				} else if (field.getValue() instanceof Integer) {
					Integer num = (Integer) field.getValue();
					size = new BigDecimal(num);
				} else {
					size = (BigDecimal) field.getValue();
				}
			}

			if (size == null) {
				size = new BigDecimal(0);
			}
			return size;
		} catch (Throwable t) {
			if (t instanceof DataBaseException)
				throw (DataBaseException) t;
			else
				throw new DataBaseException("An unexpected error occured while executing query to get used memory size", t);
		} finally {
			logger.trace("OUT");
		}
	}

	// public BigDecimal approximateUsedMemorySize() {
	// BigDecimal totalSize = new BigDecimal();
	// Iterator it = cacheRegistry.entrySet().iterator();
	// while (it.hasNext()) {
	// BigDecimal size = null;
	// Map.Entry<String,String> entry = (Map.Entry<String,String>)it.next();
	// String signature = entry.getValue();
	// String query = " select * from " + signature;
	// IDataStore dataStore = dataSource.executeStatement(query, 0, 0);
	// DataStore ds = (DataStore) dataStore;
	// BigDecimal rowWeight = getRowWeight(ds.getRecordAt(0), ds.getMetaData());
	// size = rowWeight.multiply(new BigDecimal(ds.getRecordsCount())) ;
	// logger.debug("Dimension stimated for cached object "+ signature +" [rowWeight*rows]: " + size + " ["+rowWeight+" * "+ds.getRecordsCount()+"]");
	// totalSize = totalSize.add(size);
	// }
	// return totalSize;
	// }

	public abstract String getUsedMemorySizeQuery(String schema, String tableNamePrefix);
}
