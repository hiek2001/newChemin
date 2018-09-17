package com.kh.chemin.admin.controller;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.kh.chemin.admin.model.service.AdminService;
import com.kh.chemin.common.MallPageBar;
import com.kh.chemin.common.PlacePageBar;
import com.kh.chemin.community.controller.CommunityController;
import com.kh.chemin.mall.model.vo.Product;
import com.kh.chemin.map.model.vo.Place;
import com.kh.chemin.map.model.vo.PlaceAttachment;
import com.kh.chemin.map.model.vo.PlaceMenu;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class AdminController {
	private Logger logger=LoggerFactory.getLogger(CommunityController.class);
	
	@Autowired
	private AdminService service;
	
	//회원관리 페이지
	@RequestMapping("/admin/adminPage.do")
	public String adminPage() {
		return "admin/adminPage";
	}
	
	//장소요청내역 페이지
	@RequestMapping("/admin/adminPlaceList.do")
	public String adminPlaceList(Model model,char plaStatus) {
		/*Map<String, Object> map = new HashMap<String, Object>();
		map.put("plaStatus", plaStatus);
		List<Place> plaList = service.adminPlaceList(map);*/
		/*model.addAttribute("plaList", plaList);*/
		model.addAttribute("plaStatus", plaStatus);
		return "admin/adminPlaceList";
	}
	
	//장소 승인상태에 따른 리스트
	@RequestMapping(value="/admin/placeStatusList.do",produces = "application/text; charset=utf8")
	@ResponseBody
	public String placeStatusList(char plaStatus,String plaCategory,@RequestParam(value="cPage",required=false,defaultValue="1") int cPage) throws Exception {
		int numPerPage =6;
		
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> map1 = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = null;
		
		
		map.put("plaStatus", plaStatus);
		map.put("plaCategory", plaCategory);
		
		List<Place> plaList = service.adminPlaceList(map,cPage,numPerPage);
		
		//승인상태 글 갯수
		int totalCount = service.selectPlaceCount(map);
		
		String pageBar = PlacePageBar.getPagePlace(cPage, numPerPage, totalCount);
		map1.put("plaList", plaList);
		map1.put("pageBar",  pageBar);
		jsonStr = mapper.writeValueAsString(map1);
		return jsonStr;
	}
	
	//상품관리 페이지
	@RequestMapping("/admin/adminProductList.do")
	public String memberList() {
		return "admin/adminProductList";
	}
	
	//상품등록페이지
	@RequestMapping("/admin/adminProductView.do")
	public String productView(Model model) {
		List<Map<String, String>> list = service.selectMallCate();
		model.addAttribute("cate", list);
		return "admin/adminProductEnroll";
	}
	
	//장소 상세 페이지(모달)
	@RequestMapping(value="/admin/adminPlaDetailList.do",produces = "application/text; charset=utf8")
	@ResponseBody
	public String adminPlaDetailList(int plaNo, Model model) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = null;
		
		List<PlaceAttachment> attachList = service.selectAttachList(plaNo);
		List<PlaceMenu> menuList = service.selectMenuList(plaNo);
		
		
		map.put("attachList", attachList);
		map.put("menuList", menuList);

		jsonStr = mapper.writeValueAsString(map);
		return jsonStr;
	}
	
	//장소삭제
	@RequestMapping("/admin/adminPlaceDelete.do")
	public ModelAndView placeDelete(int plaNo)
	{
		int result = service.placeDelete(plaNo);
				
		String msg="";
		String loc="";
		
		if(result>0) {
			msg="장소가 삭제되었습니다.";
		}else {
			msg="장소가 삭제 되지 않았습니다.";
		}
		loc="/admin/adminPlaceList.do?plaStatus="+'N';
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("msg", msg);
		mv.addObject("loc", loc);
		mv.addObject("result", result);
		mv.setViewName("common/msg");
		return mv;
	}
	
	//장소 상태 변경
	@RequestMapping("/admin/adminPlaceStatus.do")
	public ModelAndView placeStatus(int plaNo, char plaStatus)
	{
		Map<String, Object> map = new HashMap<>();
		map.put("plaNo", plaNo);
		map.put("plaStatus", plaStatus);
		int result = service.plaStatusChange(map);
		
		String msg="";
		String loc="";
		
		if(plaStatus=='Y') {
			if(result>0) {
				msg="승인되었습니다.";
			}else {
				msg="승인이 되지 않았습니다.";
			}
		}else if(plaStatus=='N') {
			if(result>0) {
				msg="취소되었습니다.";
			}else {
				msg="취소 되지 않았습니다.";
			}
		}
		
		loc="/admin/adminPlaceList.do?plaStatus="+'N';
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("msg", msg);
		mv.addObject("loc", loc);
		mv.addObject("result", result);
		mv.setViewName("common/msg");
		return mv;
	}
	
	//승인 거절메세지
	@RequestMapping("/admin/adminReMsg.do")
	public ModelAndView adminReMsg(int plaNo, String plaReMsg, char plaStatus) 
	{
		Map<String, Object> map = new HashMap<>();
		map.put("plaNo", plaNo);
		map.put("plaReMsg", plaReMsg);
		map.put("plaStatus", plaStatus);
		int result = service.adminReMsg(map);
		
		String msg="";
		String loc="";

			if(result>0) {
				msg="승인 거절되었습니다.";
			}else {
				msg="승인 거절되지 않았습니다.";
		}
		
			loc="/admin/adminPlaceList.do?plaStatus="+'N';
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("msg", msg);
		mv.addObject("loc", loc);
		mv.addObject("result", result);
		mv.setViewName("common/msg");
		return mv;
	}
	
	@RequestMapping("/admin/adminProductData.do")
	public void adminPData(@RequestParam(value="cPage",required=false,defaultValue="1") int cPage, HttpServletResponse response) throws Exception {
		int numPerPage = 10;
		int totalCount = service.selectProductCount();
		String pageBar = MallPageBar.getPageAdmin(cPage, numPerPage, totalCount);
		List<Map<String, Object>> list = service.selectProductList(cPage, numPerPage);
		
		JSONArray jsonArr = new JSONArray();
		jsonArr.add(list);
		jsonArr.add(pageBar);
		
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.print(jsonArr);
	}
	
	@RequestMapping(value="/admin/productEnroll.do", method = RequestMethod.POST)
	public String productEnroll(Product product, @RequestParam("mainImg") MultipartFile mainImg, HttpServletRequest request) {
		// 대표이미지 저장경로 지정 및 서버에 이미지 저장
		String saveDir = request.getSession().getServletContext().getRealPath("/resources/upload/productImg");
		
		if(!mainImg.isEmpty()) {
			String oriImg = mainImg.getOriginalFilename();
			// 확장자
			String ext = oriImg.substring(oriImg.lastIndexOf(".")+1);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String today = sdf.format(new Date(System.currentTimeMillis()));
			String reImg = today+"_"+""+"."+ext;
		}
		
		return "";
	}
	
}
