package hoge.app.welcome;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;

/**
 * Handles requests for the application home page.
 */
@Controller
public class ThymeleafQuickReferenceController {

	private static final Logger logger = LoggerFactory.getLogger(ThymeleafQuickReferenceController.class);

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = { RequestMethod.GET, RequestMethod.POST })
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);

		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);

		String formattedDate = dateFormat.format(date);

		model.addAttribute("serverTime", formattedDate);

		return "welcome/home";
	}

	@GetMapping("/actionPath")
	public String dispItemList(Model model, HttpServletRequest request, @RequestParam("paramName") String varName) {
		// HTTP Headers --------------------
		Map<String, String> httpHeaderMap = new HashMap<String, String>();
		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			System.out.println(key + ":" + value);
			httpHeaderMap.put(key, value);
		}

		// String userName = request.getUserPrincipal().getName();

		ActionForm form = new ActionForm();
		List<Item> itemList =  new ArrayList<Item>();// 項目一覧;
		Set<String> itemGroupNameSet = new LinkedHashSet<>();

		List<Map<String, Object>> queryResult = queryItemList();
		
		for (Map row : queryResult) {
			Item item = new Item();
			item.setId(Integer.parseInt(row.get("col_item_id").toString()));
			String itemGrpName = String.valueOf(row.get("col_item_grp_name"));
			item.setItemGrpName(itemGrpName);
			item.setItemName(String.valueOf(row.get("col_item_name")));
			item.setItemDisc(String.valueOf(row.get("col_item_disc")));
			item.setCurrentHogeCount(Integer.parseInt(row.get("col_cur_hoge_cnt").toString()));
			item.setTargetHogeCount(Integer.parseInt(row.get("col_tar_hoge_cnt").toString()));
			item.setHogePercent(String.valueOf(row.get("col_hoge_pcnt").toString()));
			item.setSelectedValue("1");
			itemGroupNameSet.add(itemGrpName);
		}
		form.setItemGroupNameSet(itemGroupNameSet);
		form.setItemList(itemList);
		model.addAttribute(form);
		return "thymeleafQuickReference";
	}

	private List<Map<String, Object>> queryItemList() {
		// TODO Auto-generated method stub
		return null;
	}

	public @Data class Item {
		int id;
		String itemGrpName;
		String itemName;
		String itemDisc;
		int currentHogeCount;
		int targetHogeCount;
		String hogePercent;
		String selectedValue;

		@Override
		public String toString() {
			String str = 
					"itemName: " + itemName + "\n" + 
					"itemDisc: " + itemDisc + "\n" + 
					"currentHogeCount: " + currentHogeCount + "\n" + 
					"";
			return str;
		}
	}
	
	public @Data class ActionForm {
		Set<String> itemGroupNameSet;
		List<Item> itemList;
		
	}
}
