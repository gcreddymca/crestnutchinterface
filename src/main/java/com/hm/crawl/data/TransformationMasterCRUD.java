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

import com.hm.crawl.data.vo.TransformationVO;

/**
 * This class is CRUD operation for Transformation_Master Table and
 * Segment_Transformation table.It handles operation for
 * creating,deleting,updating,reading transformations .
 * 
 */
public class TransformationMasterCRUD {

	private final static Logger logger = LoggerFactory.getLogger(TransformationMasterCRUD.class);

	/**
	 * This method is responsible to read transformations for segment.
	 * 
	 * @param segmentId
	 * @return List<TransformationVO>
	 * @throws Exception
	 */
	public List<TransformationVO> readTransformationsForSegment(String segmentId, Connection conn)
			throws Exception {
        boolean connCreated = false;
		if(conn == null) {
			conn = JDBCConnector.getConnection();
			connCreated= true;
		}
		List<TransformationVO> result = new ArrayList<TransformationVO>();

		if (conn == null) {
			logger.error("Unable to get connection from DB: in readTransformationsForSegment() process:");
			result = null;
			return result;
		}
		if (segmentId == null) {
			logger.info("Segment Id is null");
			result = null;
			return result;
		}
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String query = "SELECT master.transformation_type,segment.transformation_priority from transformation_master master LEFT OUTER JOIN segment_transformation segment ON segment.transformation_id = master.transformation_id  where segment.SEGMENT_ID = "
					+ segmentId + "order By segment.transformation_priority";

			boolean success = stmt.execute(query);
			if (success) {
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {
					TransformationVO vo = new TransformationVO();
					vo.setTransformationType(rs
							.getString("TRANSFORMATION_TYPE"));
					vo.setTransformationPriority(rs
							.getString("TRANSFORMATION_PRIORITY"));
					result.add(vo);
				}
			}
		} catch (SQLException e) {
			result = null;
			logger.error("Error while fetching details from  in SEGMENT_TRANSFORMATION in readTransformationsForSegment() process: "+ e.getMessage());
			throw new Exception("Could not read tranformation for segment");
		} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
					if(connCreated){
					   conn.close();
					}
				 } catch (SQLException e) {
					logger.error("Error while closing connection in readTransformationsForSegment() process:" + e.getMessage());
				}
		}
		return result;
	}

	/**
	 * This method creates new transformation in Transformation_Master table.
	 * 
	 * @param vo
	 * @param segmentId
	 * @return boolean
	 * @throws Exception
	 */
	public boolean create(TransformationVO vo) throws Exception {
		Connection conn = JDBCConnector.getConnection();
		if (conn == null) {
			logger.error("Unable to get connection from DB: in create() process:");
			return false;
		}
		PreparedStatement stmt = null;
		conn.setAutoCommit(false);
		Statement stmtNextVal = null;
		try {
			stmtNextVal = conn.createStatement();
			stmtNextVal.execute("SELECT TRANSFORMATION_SEQ.NEXTVAL FROM DUAL");
			ResultSet rs = stmtNextVal.getResultSet();
			rs.next();
			int seqNum = rs.getInt("NEXTVAL");

			String query = "INSERT INTO TRANSFORMATION_MASTER (TRANSFORMATION_ID,TRANSFORMATION_TYPE) VALUES(?,?)";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, seqNum);
			stmt.setString(2, vo.getTransformationType());
			stmt.execute();
			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			logger.error("Error while creating row in TRANSFORMATION_MASTER table in create() process: " + e.getMessage());
			throw new Exception("Could not create transformation");
		} finally {
				try {
					if (stmtNextVal != null) {
						stmtNextVal.close();
					}
					if (stmt != null) {
						stmt.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					logger.error("Error while closing connection in create() process: " + e.getMessage());
				}
		}
		return true;
	}

	/**
	 * This method creates updates Transformation_Master table.
	 * 
	 * @param vo
	 * @param segmentId
	 * @return boolean
	 * @throws Exception
	 */
	public boolean update(TransformationVO vo, String updated_transformation)
			throws Exception {
		Connection conn = JDBCConnector.getConnection();
		if (conn == null) {
			logger.error("Unable to get connection from DB: in update() process:");
			return false;
		}
		PreparedStatement stmt = null;
		conn.setAutoCommit(false);
		try {
			String query = "UPDATE TRANSFORMATION_MASTER SET TRANSFORMATION_TYPE='"
					+ vo.getTransformationType()
					+ "' where transformation_type='"
					+ updated_transformation
					+ "'";
			stmt = conn.prepareStatement(query);
			stmt.execute();
			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			logger.error("Error while updating TRANSFORMATION_MASTER table in update() process: " + e.getMessage());
			throw new Exception("Could not update transformation");
		} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					logger.error("Error while closing connection in update() process: " + e.getMessage());
				}
		}
		return true;
	}

	/**
	 * This method is responsible to delete transformation from segment.
	 * 
	 * @param vo
	 * @return boolean
	 * @throws Exception
	 */
	public boolean deleteFromSegment(TransformationVO vo) throws Exception {
		Connection conn = JDBCConnector.getConnection();
		if (conn == null) {
			logger.error("Unable to get connection from DB: deleteFromSegment() process:");
			return false;
		}
		PreparedStatement stmt = null;
		conn.setAutoCommit(false);
		try {
			String query = "delete from segment_transformation where transformation_id = (select transformation_id from transformation_master where transformation_type='"
					+ vo.getTransformationType() + "')";
			stmt = conn.prepareStatement(query);
			stmt.execute();
			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			logger.error("Error while deleting transformation for segment in deleteFromSegment() process: " + e.getMessage());
			throw new Exception("Error deleting transformation from segment.");
		} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					logger.info("Error while closing connection" + e);
				}
		}
		return true;
	}

	/**
	 * This method is responsible to delete transformation from
	 * transformation_master table.
	 * 
	 * @param vo
	 * @return boolean
	 * @throws Exception
	 */
	public boolean deleteFromMaster(TransformationVO vo) throws Exception {
		Connection conn = JDBCConnector.getConnection();
		if (conn == null) {
			logger.info("Connection not found. Could not delete row in TRANSFORMATION_MASTER");
			return false;
		}
		PreparedStatement stmt = null;
		conn.setAutoCommit(false);
		try {

			String query = "delete from transformation_master where transformation_type = '"
					+ vo.getTransformationType() + "'";
			stmt = conn.prepareStatement(query);
			stmt.execute();

			conn.commit();

		} catch (SQLException e) {
			conn.rollback();

			logger.info("Error deleting transformation" + e);
			throw new Exception("Error deleting transformation.");

		} finally {
			if (stmt != null) {

				try {
					stmt.close();

					conn.close();

				} catch (SQLException e) {
					logger.info("Error while closing connection" + e);
				}

			}
		}
		return true;
	}

	/**
	 * This method updates transformation for segment.
	 * 
	 * @param vo
	 * @param segmentId
	 * @return boolean
	 * @throws Exception
	 */
	public boolean update(TransformationVO vo, int segmentId) throws Exception {
		Connection conn = JDBCConnector.getConnection();
		boolean update = false;
		if (conn == null) {
			logger.info("Connection not found. Could not create row in SEGMENT_TRANSFORMATION");
			return false;
		}
		Statement stmt = null;
		PreparedStatement preStmt = null;
		conn.setAutoCommit(false);
		int transformationId = 0;
		try {
			String query = "SELECT TRANSFORMATION_ID FROM TRANSFORMATION_MASTER WHERE TRANSFORMATION_TYPE='"
					+ vo.getTransformationType().trim() + "'";
			stmt = conn.createStatement();
			boolean success = stmt.execute(query);
			if (success) {
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {
					transformationId = rs.getInt("TRANSFORMATION_ID");
				}
			}

			query = "UPDATE SEGMENT_TRANSFORMATION SET TRANSFORMATION_PRIORITY="
					+ vo.getTransformationPriority()
					+ "WHERE TRANSFORMATION_ID=" + transformationId;
			preStmt = conn.prepareStatement(query);

			preStmt.execute();
			conn.commit();

		} catch (SQLException e) {
			conn.rollback();
			logger.info("Error while creating row in SEGMENT_TRANSFORMATION"+ e);
			throw new Exception("Error creating transformation for segment.");

		} finally {
			if (stmt != null) {
				try {
					stmt.close();
					conn.close();
				} catch (SQLException e) {
					logger.info("Error while closing connection" + e);
				}
			}
		}
		return true;
	}

	/**
	 * This method adds new transformation for segment.
	 * 
	 * @param vo
	 * @param segmentId
	 * @return boolean
	 * @throws Exception
	 */
	public boolean add(TransformationVO vo, int segmentId) throws Exception {
		Connection conn = JDBCConnector.getConnection();
		boolean update = false;
		if (conn == null) {
			logger.info("Connection not found. Could not create row in SEGMENT_TRANSFORMATION");
			return false;
		}
		Statement stmt = null;
		PreparedStatement preStmt = null;
		conn.setAutoCommit(false);
		int transformationId = 0;
		try {
			String query = "SELECT TRANSFORMATION_ID FROM TRANSFORMATION_MASTER WHERE TRANSFORMATION_TYPE='"
					+ vo.getTransformationType().trim() + "'";
			stmt = conn.createStatement();
			boolean success = stmt.execute(query);
			if (success) {
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {
					transformationId = rs.getInt("TRANSFORMATION_ID");
				}
			}

			// Add new transformation for segment.
			query = "INSERT INTO SEGMENT_TRANSFORMATION (SEGMENT_ID,TRANSFORMATION_ID,TRANSFORMATION_PRIORITY) VALUES(?,?,?)";
			preStmt = conn.prepareStatement(query);
			preStmt.setInt(1, segmentId);
			preStmt.setInt(2, transformationId);
			preStmt.setString(3, vo.getTransformationPriority());

			preStmt.execute();
			conn.commit();

		} catch (SQLException e) {
			conn.rollback();
			logger.info("Error while creating row in SEGMENT_TRANSFORMATION"+ e);
			throw new Exception("Error creating transformation for segment.");

		} finally {
			if (stmt != null) {
				try {
					stmt.close();
					conn.close();
				} catch (SQLException e) {
					logger.info("Error while closing connection" + e);
				}
			}
		}
		return true;
	}
	
	/**
	 * This method reads transformations available in transformation_master
	 * table.
	 * 
	 * @return List<TransformationVO>
	 * @throws Exception
	 */
	public List<TransformationVO> read() throws Exception {
		Connection conn = JDBCConnector.getConnection();
		List<TransformationVO> result = new ArrayList<TransformationVO>();
		if (conn == null) {
			logger.info("Connection not found. Could not read from Transformation_master");
			return result;
		}

		Statement stmt = null;
		try {

			String query = "SELECT transformation_type from transformation_master";
			stmt = conn.createStatement();
			boolean success = stmt.execute(query);
			if (success) {
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {
					TransformationVO vo = new TransformationVO();
					vo.setTransformationType(rs
							.getString("transformation_type"));
					result.add(vo);

				}
			}
		} catch (SQLException e) {
			logger.info("Error while fetching row in transformation_master" + e);
			throw new Exception(
					"Error while fetching row in transformation_master");
		} finally {
			if (stmt != null) {

				try {
					stmt.close();
					conn.close();
				} catch (SQLException e) {
					logger.info("Error while closing connection" + e);
				}

			}
		}
		return result;

	}

	/**
	 * This method checks for unique priority in Segment_Transformation Table
	 * for segment.
	 * 
	 * @param priority
	 * @return boolean
	 * @throws SQLException
	 */
	public boolean checkforUniquePriority(TransformationVO vo, int segmentId)
			throws SQLException {
		Connection conn = JDBCConnector.getConnection();
		Statement stmt = null;
		boolean result = false;
		try {
			stmt = conn.createStatement();
			boolean success = stmt
					.execute("select * from segment_transformation where TRANSFORMATION_PRIORITY ="
							+ vo.getTransformationPriority()
							+ "and SEGMENT_ID=" + segmentId);
			ResultSet rs = stmt.getResultSet();
			if (success) {
				if (rs.next())
					result = false;
				else
					result = true;
			}
		} catch (SQLException sqlEx) {
			logger.error(sqlEx.getMessage());
		} finally {
			if (null != conn)
				conn.close();
			if (null != stmt)
				stmt.close();
		}
		return result;
	}
	
	
	public List<TransformationVO> load() throws Exception {
		Connection conn = JDBCConnector.getConnection();
		List<TransformationVO> transList = new ArrayList<TransformationVO>();
		if (conn == null) {
			logger.info("Connection not found. Could not read from Transformation_master");
			return transList;
		}
		Statement stmt = null;
		ResultSet rs = null;
		conn.setAutoCommit(false);
		try {

			String query = "SELECT * from transformation_master";
			stmt = conn.createStatement();
			boolean success = stmt.execute(query);
			if (success) {
				rs = stmt.getResultSet();
				while (rs.next()) {
					TransformationVO vo = new TransformationVO();
					vo.setTransformationType(rs.getString("transformation_type"));
					vo.setTransformationId(rs.getString("TRANSFORMATION_ID"));
					transList.add(vo);
				}
			}
		} catch (SQLException e) {
			logger.info("Error while fetching row in transformation_master" + e);
			throw new Exception(
					"Error while fetching row in transformation_master");
		} finally {
			

				try {
					if (stmt != null) {
						stmt.close();
					}
					if(conn != null){
						conn.close();
					}
					if(rs != null){
						rs.close();
					}
				} catch (SQLException e) {
					logger.info("Error while closing connection" + e);
				}

			}
		return transList;

	}
	
	/**
	 * This method is responsible to delete transformation from segment.
	 * 
	 * @param vo
	 * @return boolean
	 * @throws Exception
	 */
	public boolean deleteFromSegment(TransformationVO vo,String segmentId) throws Exception {
		Connection conn = JDBCConnector.getConnection();
		if (conn == null) {
			logger.error("Unable to get connection from Database: in deleteFromSegment method block:");
			return false;
		}
		else{
			PreparedStatement stmt = null;
			conn.setAutoCommit(false);
			try {
				String query = "delete from segment_transformation where transformation_id = (select transformation_id from transformation_master where transformation_type='"
						+ vo.getTransformationType() + "') and SEGMENT_ID="+segmentId;
				stmt = conn.prepareStatement(query);
				stmt.execute();
				conn.commit();
				return true;
			} catch (SQLException e) {
				conn.rollback();
				logger.error("Error while executing query in deleteFromSegment method block:" + e.getMessage());
				throw new Exception("Error deleting transformation from segment.");
			} finally {
				try {
					if(stmt != null){
						stmt.close();
					}
					if(conn != null){
						conn.close();
					}
				} catch (SQLException e) {
					logger.error("Error while closing connection in deleteFromSegment method block:" + e.getMessage());
				}
			}
		}
	}

}
