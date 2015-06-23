package com.indiScene.performBoard.service;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.indiScene.performBoard.dao.PerformBoardDaoImpl;
import com.indiScene.performBoard.dto.PerformBoardDto;

@Component
public class PerformBoardServiceImpl implements PerformBoardService {
	
	@Autowired
	private PerformBoardDaoImpl boardDao;
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	public void boardWrite(ModelAndView mav){
		Map<String, Object> map = mav.getModelMap();
		HttpServletRequest request = (HttpServletRequest) map.get("request");
		
		String board_num = "0";
		int group_num = 1;
		int seq_num = 0;
		int seq_level = 0;
	
		if(request.getParameter("board_num") != null){
	
			board_num = request.getParameter("board_num");
			group_num = Integer.parseInt(request.getParameter("group_num"));
			seq_num = Integer.parseInt(request.getParameter("seq_num"));
			seq_level = Integer.parseInt(request.getParameter("seq_level"));
		}
		
		//logger.info("ch =>" + boardNumber + "||" + groupNumber + "||" + sequenceNumber + "||" + sequenceLevel);
		
		mav.addObject("board_num" , board_num);
		mav.addObject("group_num" , group_num);
		mav.addObject("seq_num" , seq_num);
		mav.addObject("seq_level" , seq_level);
		
		mav.setViewName("board/write");
	}
	
	public void writeOk(ModelAndView mav){
		Map<String, Object> map = mav.getModelMap();
		MultipartHttpServletRequest request = (MultipartHttpServletRequest)map.get("request");
		PerformBoardDto boardDto = (PerformBoardDto) map.get("boardDto");
		
		boardDto.setRegister_date(new Date());
		boardDto.setCount(0);
		
		String file_path = null;
		String file_name = null;
		
		fileBoardWriteNumber(boardDto);
		/*for(int i = 1; i <= 10; i++){
			logger.info("--" + request.getFile("file"+i));
		}*/
		
		for(int i = 1; i < 10; i++){
			MultipartFile upFile = request.getFile("file" + i);
			String fileName = upFile.getOriginalFilename();
		
			
			
			String timeName = System.currentTimeMillis() + "_" + fileName ;
			long fileSize = upFile.getSize();
			
			logger.info("-- fileName : " + fileName);
			logger.info("-- timeName : " + timeName);
			logger.info("-- fileSize : " + fileSize);
			
			if(fileSize != 0){
				try{
					//?��??경로
					//String dir="C:\\mavenSpring\\workspace\\mavenHomePage\\src\\main\\webapp\\performResource";
					
					//?��??경로
					String dir=request.getSession().getServletContext().getRealPath("/performResource");
					
					logger.info("ch dir : " + dir);
					
					File file = new File(dir, timeName);
					upFile.transferTo(file);	//?��?��?��?��?�� ?��출력?�� ?��료됨
					
					
				file_path += file.getAbsolutePath() + ",";
				file_name += fileName + ",";
				}catch(Exception e){
					logger.info("ch File Input Ouput Error");
				}
			}
		}
		boardDto.setFile_path(file_path);
		boardDto.setFile_name(file_name);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd,hh");
		try {
			boardDto.setD_day(sdf.parse(request.getParameter("d_day1")));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//logger.info("-- "+Long.parseLong(request.getParameter("d_day1")));
		int check = boardDao.insert(boardDto);
		logger.info("ch check : " + check);
		
		mav.addObject("check" , check);
		//mav.setViewName(null);
		mav.setViewName("performBoard/writeOk");
		
	}
	
	public void fileBoardWriteNumber(PerformBoardDto boardDto){
	
		String board_num = boardDto.getBoard_num();
		int group_num = boardDto.getGroup_num();
		int seq_num = boardDto.getSeq_num();
		int seq_level = boardDto.getSeq_level();
		
		//logger.info("ch boardWriteNumber =>" + boardNumber + "||" + groupNumber + "||" + sequenceNumber + "||" + sequenceLevel);
		
		int max = 0;
		if(board_num == "0"){
			//Root
			max=boardDao.boardGroupNumberMax();
			//logger.info("ch max : " + max);
			if(max != 0){
				max = max+1;
			}else{
				max = boardDto.getGroup_num();
			}
			
			group_num=max;
			seq_num=boardDto.getSeq_num();
			seq_level = boardDto.getSeq_level();
		}else{
			//?���?
			HashMap<String, Integer> hMap = new HashMap<String, Integer>();
			hMap.put("group_num", group_num);
			hMap.put("seq_num", seq_num);
			
			boardDao.boardGroupNumberUpdate(hMap);
			seq_level+=1;
			seq_num+=1;
		}
		
		boardDto.setGroup_num(group_num);
		boardDto.setSeq_num(seq_num);
		boardDto.setSeq_level(seq_level);
		
		logger.info("--"+group_num + "," + seq_num + "," + seq_level);
		
		//logger.info("ch max : " + max);
	}
	
	public void list(ModelAndView mav){
		Map<String, Object> map = mav.getModelMap();
		HttpServletRequest request = (HttpServletRequest) map.get("request");
		
		int boardSize = 10;
		String pageNumber = request.getParameter("pageNumber");
		if(pageNumber == null) pageNumber = "1";
		
		int currentPage = Integer.parseInt(pageNumber);
		int startRow = (currentPage - 1)* boardSize + 1;
		int endRow = currentPage*boardSize;
		
		int count = boardDao.getBoardCount();
		logger.info("ch count : " + count);
		
		List<PerformBoardDto> list = boardDao.getBoardList(startRow, endRow);
		logger.info("ch list : " + list.size());
		
		mav.addObject("boardList", list);
		mav.addObject("count", count);
		mav.addObject("boardSize", boardSize);
		mav.addObject("currentPage", currentPage);
		
		mav.setViewName("performBoard/list");
		//String viewPage="../fileBoard/list.jsp";
		//mav.addObject("viewPage",viewPage);
		//mav.setViewName("template/index1");
				
	}
}


