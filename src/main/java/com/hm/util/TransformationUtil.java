package com.hm.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.data.TransformationMasterCRUD;
import com.hm.crawl.data.vo.TransformationVO;

public class TransformationUtil {
	public static final Logger LOG = LoggerFactory.getLogger(TransformationUtil.class);
	
	public List<String> getSegmentsTransformations(Integer segmentId) throws Exception {
		List<String> transformationTypes = new ArrayList<String>();
		if(segmentId != 0){
			List<TransformationVO> transformationsList = new TransformationMasterCRUD().readTransformationsForSegment(segmentId.toString(),null);
			if(transformationsList != null) {
				Iterator<TransformationVO> itr = transformationsList.iterator();
				TransformationVO transVO = null;
				while(itr.hasNext()) {
					transVO = itr.next();
					transformationTypes.add(transVO.getTransformationType());
				}
			}
		}else{
			LOG.error("Segment id is null in getSegmentsTransformations() method: "); 
		}
		return transformationTypes;
	}
	public static boolean checkforValidation(TransformationVO vo, boolean validatePriority, int segmentId) {
		List<String> errorMessages = new ArrayList<String>();
		if (!isNullorEmpty(vo.getTransformationType())) {
			errorMessages.add("Please provide transformation type.");
		}
		if (validatePriority) {
			try {
				if (vo.getTransformationPriority() == "") {
					errorMessages
							.add("Please provide priority for Transformation");
				} else if (Integer.parseInt(vo.getTransformationPriority()) < 0) {
					errorMessages
							.add("Please provide priority for Transformation greater than zero");
				}
				if (!new TransformationMasterCRUD().checkforUniquePriority(vo,
						segmentId)) {
					errorMessages.add("Please add unique priority ");
				}

			} catch (NumberFormatException e) {
				errorMessages
						.add("Please provide valid Integer value for Priority");
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}

		if (errorMessages.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isNullorEmpty(String param) {
		if (param == null || param.trim() == "" || param.isEmpty())
			return false;
		else
			return true;
	}
	
	
}
