package springbook.user.sqlservice;

public class SqlAdminService implements AdminEventListener {

	private static final String KEY_ID = null;
	private static final String SQL_ID = null;
	private UpdatableSqlRegistry updatableSqlRegistry;

	public void setUpdatabaleSqltableRegistry(UpdatableSqlRegistry updatableSqlRegistry) {
		this.updatableSqlRegistry = updatableSqlRegistry;
	}

	public void updateEventListner(UpdateEvent event) {
		this.updatableSqlRegistry.updateSql(event.get(KEY_ID), event.get(SQL_ID));
	}

}
