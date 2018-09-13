package com.kh.chemin.map.controller;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.chemin.community.controller.CommunityController;
import com.kh.chemin.community.model.service.CommunityService;
import com.kh.chemin.map.model.service.PlaceService;
import com.kh.chemin.map.model.vo.Place;
import com.kh.chemin.map.model.vo.PlaceAttachment;
import com.kh.chemin.map.model.vo.PlaceMenu;
import com.kh.chemin.map.model.vo.PlaceReview;
import com.sun.org.glassfish.gmbal.ParameterNames;

@Controller
public class MapController {
	
	private Logger logger=LoggerFactory.getLogger(MapController.class);

	@Autowired
	private PlaceService service;
	
	//지도를 보여주는 화면
	@RequestMapping("/map/mapView.do")
	public String mapView(Model model) {
		List<Place> plaList = service.placeList();
		model.addAttribute("plaList", plaList);
		return "/map/mapView";
	}
	
	//장소 상세정보를 보여주는 화면
	@RequestMapping("/map/placeInfo.do")
	public String placeinfo(String plaNo, Model model, HttpServletRequest reqeust) {
		int no = Integer.parseInt(reqeust.getParameter("plaNo"));
		Place place = service.placeSelect(no);
		List<PlaceAttachment> attachList = service.selectAttachList(no);
		List<PlaceMenu> menuList = service.selectMenuList(no);
		model.addAttribute("place", place);
		model.addAttribute("attachList", attachList);
		model.addAttribute("menuList", menuList);
		return "/map/placeInfo";
	}
	
	//장소를 등록하는 화면
	@RequestMapping("/map/placeReg.do")
	public String register() {
		return "/map/placeReg";
	}

	
	//장소 등록
	@RequestMapping(value="/map/placeInsert.do", method = RequestMethod.POST)
	public ModelAndView placeInsert(Place place ,@RequestParam("mainImg")MultipartFile mainImg,@RequestParam("file")MultipartFile[] file,HttpServletRequest request,String[] menuName,String[] menuPrice,String[] menuCheck, String phoneFirst, String phoneMiddle, String phoneEnd,String postCode, String roadAddr, String jibunAddr,
							  String day, String startTime, String endTime,String subContent,String keyword1,String keyword2, String keyword3, String keyword4, String keyword5) {
		
		String phone=phoneFirst+"-"+phoneMiddle+"-"+phoneEnd;
		String address=roadAddr+"/"+postCode+"/"+jibunAddr;
		String time=day+"/"+startTime+"/"+endTime+"/"+subContent;
		String keyword = keyword1+" "+keyword2+" "+keyword3+" "+keyword4+" "+keyword5;
		place.setPlaPhone(phone);
		place.setPlaAddr(address);
		place.setPlaTime(time);
		place.setPlaKeyword(keyword);
		place.setUserId(place.getUserId());

		System.out.println(place);
		//대표이미지 저장경로 지정 및 서버에 이미지 저장
		String saveDirMain = request.getSession().getServletContext().getRealPath("/resources/upload/place/main");
		if(!mainImg.isEmpty()) {	
			String orImg = mainImg.getOriginalFilename();
			String reImg = orImg;
			try {
				mainImg.transferTo(new File(saveDirMain+"/"+reImg));
			} catch (IOException e) {
				e.printStackTrace();
			} 
			place.setOrImg(orImg);
			place.setReImg(reImg);
		}
		
		//배열수 만큼 가격정보 db에 넣기
		List<PlaceMenu> menuList = new ArrayList();
		for(int i=0; i<menuName.length; i++) {
			if(!menuName[i].isEmpty()&&!menuPrice[i].isEmpty()) {
			PlaceMenu menu = new PlaceMenu();
			 menu.setMenuName(menuName[i]);
			 menu.setMenuPrice(menuPrice[i]);
			 menuList.add(menu);
			}
		}
		
		List<PlaceAttachment> attList = new ArrayList();
		String saveDirAttch = request.getSession().getServletContext().getRealPath("/resources/upload/place/attach");
		for(MultipartFile f : file) {
			if(!f.isEmpty()) {
				String attachOrImg = f.getOriginalFilename();
				String attachReImg = attachOrImg;
				try {
					f.transferTo(new File(saveDirAttch+"/"+attachReImg));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				PlaceAttachment attach = new PlaceAttachment();
				attach.setOrImg(attachOrImg);
				attach.setReImg(attachReImg);
				attList.add(attach);
			}
		}
		
		
		int result = service.placeInsert(place,menuList,attList);
		String msg="";
		String loc="";
		
		if(result>0) {
			msg="등록이 완료되었습니다.";
		}else {
			msg="등록이 되지 않았습니다.";
		}
		loc="/";
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("msg", msg);
		mv.addObject("loc", loc);
		mv.addObject("result", result);
		mv.setViewName("common/msg");
		return mv;
	}
	
	//장소 카테고리 검색
	@RequestMapping("/map/placeSearch.do")
	public String placeSearch(String plaCategory, String plaArea, Model model) {
		Map<String, String> map = new HashMap<>();
		map.put("plaCategory", plaCategory);
		map.put("plaArea", plaArea);
		
		List<Place> plaList = service.placeSearch(map);
		model.addAttribute("plaList", plaList);
		return "/map/mapView";
	}
	
	//장소 리뷰글 등록하기
	@RequestMapping(value="/map/placeInsertReview.do",produces = "application/text; charset=utf8")
	@ResponseBody
	public String placeSelect(PlaceReview review) throws Exception
	{
		Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = null;
		int result = service.placeInsertReview(review);
		map.put("result", result);
		jsonStr = mapper.writeValueAsString(map);
		return jsonStr;
	}
	
	//장소 리뷰리스트 가져오기
	@RequestMapping(value="/map/placeReviewList.do",produces = "application/text; charset=utf8")
	@ResponseBody
	public String placeSelect(int plaNo) throws Exception
	{
		Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = null;
		List<PlaceReview> reviewList = service.placeReviewList(plaNo);
		
		map.put("reviewList", reviewList);
		jsonStr = mapper.writeValueAsString(map);
		return jsonStr;
	}
	
}