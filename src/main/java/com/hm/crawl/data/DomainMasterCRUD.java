package com.hm.crawl.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.nutch.tools.JDBCConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.data.vo.DomainVO;

/**
 * This class is CRUD operation for Domain Master Table.It handles operation for
 * creating,deleting,updating,fetching domain.
 * 
 * 
 */
public class DomainMasterCRUD {
	private final static Logger logger = LoggerFactory.getLogger(DomainMasterCRUD.class);

	/**
	 * This method creates new domain in Domain Master
	 * 
	 * @param domain
	 * @return boolean
	 * @throws Exception
	 */
	public boolean create(DomainVO domain) throws Exception {
		Connection conn = JDBCConnector.getConnection();
		boolean result = false;
		if (conn == null) {
			logger.error("Unable to get connection from DB: in Domain Creation process:");
			return result;
		}
		PreparedStatement stmt = null;
		conn.setAutoCommit(false);
		try {
			String query = "INSERT INTO DOMAIN (DOMAIN_ID,DOMAIN_NAME,URL,SEED_URL,RAW_CONTENT_DIRECTORY,FINAL_CONTENT_DIRECTORY) VALUES(domain_seq.nextval,?,?,?,?,?)";
			stmt = conn.prepareStatement(query);

			stmt.setString(1, domain.getDomainName());
			stmt.setString(2, domain.getUrl());
			stmt.setString(3, domain.getSeedUrl());
			stmt.setString(4, domain.getRaw_content_directory());
			stmt.setString(5, domain.getFinal_content_directory());

			stmt.execute();
			conn.commit();
			result = true;
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error while closing connection in Domain creation process:" + e.getMessage());
			}
		} catch (Exception e) {
			logger.error("Error while creating Domain in DOMAIN table:" + e.getMessage());
			conn.rollback();
			conn.close();
		}
		return result;
	}

	/**
	 * This method reads domain details from DOMAIN Master table.
	 * 
	 * @return List<DomainVO>
	 * @throws Exception
	 */
	public List<DomainVO> read() throws Exception {
		Connection conn = JDBCConnector.getConnection();
		List<DomainVO> result = null;
		if (conn == null) {
			logger.error("Unable to get connection from DB: in Domain details fetching process:");
			return result;
		}
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String query = " SELECT * FROM DOMAIN ORDER BY DOMAIN_NAME";
			boolean success = stmt.execute(query);
			if (success) {
				ResultSet rs = stmt.getResultSet();
				result = new ArrayList<DomainVO>();
				while (rs.next()) {
					DomainVO dom = new DomainVO();
					dom.setDomainId(rs.getInt("DOMAIN_ID"));
					dom.setDomainName(rs.getString("DOMAIN_NAME"));
					dom.setUrl(rs.getString("URL"));
					dom.setSeedUrl(rs.getString("SEED_URL"));
					dom.setRaw_content_directory(rs
							.getString("raw_content_directory"));
					dom.setFinal_content_directory(rs
							.getString("final_content_directory"));
					result.add(dom);
				}
			}
		} catch (Exception e) {
			logger.error("Error while fetching domain details from DOMAIN table:" + e.getMessage());
			throw new Exception("Could not read domain details from DOMAIN table:");
		} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (Exception e) {
					logger.error("Error while closing connection in Domain details fetching process:" + e.getMessage());
				}
			}
		return result;
	}

	/**
	 * This method updates domain_master table.
	 * 
	 * @param domainVO
	 * @return boolean
	 * @throws SQLException
	 */
	public boolean update(DomainVO domainVO) throws Exception {
		Connection conn = JDBCConnector.getConnection();
		boolean result = false;
		if (conn == null) {
			logger.error("Unable to get connection from DB: in Domain update process:");
			return result;
		}
		Statement stmtBatch = null;
		Statement stmt = null;
		conn.setAutoCommit(false);
		try {
			stmtBatch = conn.createStatement();
			stmt = conn.createStatement();
			String query = "UPDATE DOMAIN set DOMAIN_NAME = '"
					+ domainVO.getDomainName() + "',URL = '"
					+ domainVO.getUrl() + "',SEED_URL = '"
					+ domainVO.getSeedUrl() + "',RAW_CONTENT_DIRECTORY='"
					+ domainVO.getRaw_content_directory()
					+ "',FINAL_CONTENT_DIRECTORY='"
					+ domainVO.getFinal_content_directory()
					+ "' where DOMAIN_ID = " + domainVO.getDomainId();
			stmtBatch.addBatch(query);
			query = "SELECT * from DOMAIN where DOMAIN_ID="
					+ domainVO.getDomainId();

			stmt.execute(query);
			stmtBatch.executeBatch();
			conn.commit();
			result = true;
		} catch (Exception e) {
			logger.error("Error while updating domain in DOMAIN table:"+e.getMessage());
			conn.rollback();
			throw new Exception("Could Not update domain_master");
		} finally {
			try {
				if (stmtBatch != null)
					stmtBatch.close();
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				logger.error("Error while closing connection in Domain Update Process:" + e.getMessage());
			}
		}
		return result;
	}

	/**
	 * This method reads domain information for the specified domainId from
	 * Domain_Master.
	 * 
	 * @param primaryKey
	 * @return DomainVO
	 * @throws Exception
	 */
	public DomainVO readByPrimaryKey(int primaryKey) throws Exception {
		Connection conn = JDBCConnector.getConnection();
		DomainVO domain = new DomainVO();
		boolean result = false;
		if (conn == null) {
			logger.error("Unable to get connection from database in readByPrimaryKey method block:");
			return domain;
		}
		else{
			PreparedStatement stmt = null;
			try {
				String query = "SELECT * from DOMAIN where DOMAIN_ID = ?";
				stmt = conn.prepareStatement(query);
				stmt.setInt(1, primaryKey);
	
				result = stmt.execute();
				ResultSet resultSet = stmt.getResultSet();
				if(resultSet.next()){
				domain.setDomainId(resultSet.getInt("DOMAIN_ID"));
				domain.setDomainName(resultSet.getString("DOMAIN_NAME"));
				domain.setSeedUrl(resultSet.getString("SEED_URL"));
				domain.setUrl(resultSet.getString("URL"));
				domain.setRaw_content_directory(resultSet
						.getString("raw_content_directory"));
				domain.setFinal_content_directory(resultSet
						.getString("final_content_directory"));
				}
				if(resultSet != null){
					resultSet.close();
				}
			} catch (Exception e) {
				logger.error("Error while executing query in readByPrimaryKey method block:" + e.getMessage());
				throw new Exception("Could Not read Domain from Doamin_Master");
			} finally {
					try {
						if (stmt != null) {
							stmt.close();
						}
						if(conn !=null){
							conn.close();
						}
					} catch (Exception e) {
						logger.error("Error while closing connection in readByPrimaryKey method block:" + e.getMessage());
					}
			}
			return domain;
		}
	}

	/**
	 * This method deletes domain from DOMAIN table.
	 * 
	 * @param domainId
	 * @return boolean
	 * @throws Exception
	 */
	public boolean delete(String domainId) throws Exception {
		Connection conn = JDBCConnector.getConnection();
		boolean result = false;
		if (conn == null) {
			logger.error("Unable to get connection from DB: in Domain delete process:");
			return result;
		}
		else{
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				conn.setAutoCommit(false);
				String query = "DELETE FROM SEGMENT_TRANSFORMATION where SEGMENT_ID IN (SELECT SEGMENT_ID FROM SEGMENT_MASTER WHERE DOMAIN_ID ='"+domainId+"')";
				stmt.execute(query);
				query = "DELETE FROM SEGMENT_HTML_PATH_PATTERN WHERE SEGMENT_ID IN (SELECT SEGMENT_ID FROM SEGMENT_MASTER WHERE DOMAIN_ID ='"+domainId+"')";
				stmt.execute(query);
				query = "DELETE FROM URL_HTML_LOC WHERE DOMAIN_ID='"+domainId+"'";
				stmt.execute(query);
				query = "DELETE FROM URL_DETAIL WHERE DOMAIN_ID='"+domainId+"'";
				stmt.execute(query);
				query = "DELETE FROM SEGMENT_MASTER WHERE DOMAIN_ID = "+ domainId;
				stmt.execute(query);
				query = "DELETE from DOMAIN where DOMAIN_ID = "+ domainId;
				stmt.execute(query);
				conn.commit();
				result = true;
			} catch (Exception e) {
				conn.rollback();
				logger.error("Error while deleting domain form  DOMAIN table:" + e.getMessage());
				throw new Exception("Could not delete domain ");
			} finally {
					try {
						if (stmt != null) {
							stmt.close();
						}
						if(conn != null){
							conn.close();
						}
					} catch (Exception e) {
						logger.error("Error while closing connection in delete Domain process:" + e.getMessage());
					}
				}
		}
		return result;
	}

	/**
	 * Fetching DOMAIN_ID from DOMAIN table by passing DOMAIN_NAME
	 * @param vo
	 * @return DOMAIN_ID
	 */
	public int getDomainId(DomainVO vo) {
		Connection conn = JDBCConnector.getConnection();
		int result = 0;
		if (conn == null) {
			logger.error("Unable to get connection from DB: in getDomainId() process:");
			return 0;
		}
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String query = " SELECT DOMAIN_ID FROM DOMAIN WHERE DOMAIN_NAME="+ "'" + vo.getDomainName() + "'";
			boolean success = stmt.execute(query);
			if (success) {
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {
					result = rs.getInt("DOMAIN_ID");
				}
			}
		} catch (Exception e) {
			logger.error("Error while fetching row from  DOMAIN table in getDomainId() process:" + e.getMessage());
		} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (Exception e) {
					logger.error("Error while closing connection in getDomainId() process:" +e.getMessage());
				}
			}
		return result;
	}
}
