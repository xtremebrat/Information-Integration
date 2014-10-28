package com.hw1.scraper;

import java.io.FileWriter;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Scraper {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		Document doc;
		int i = 0;
		String url = "http://classes.toledomuseum.org:8080/emuseum/view/objects/asitem/199/0/invno-asc?t:state:flow=47e153ec-ac55-45da-a9fb-c7afad985d6f";
		String nextUrl = "";
		String title = "", authb = "", img = "", date = "", dim = "", med = "", obj = "", cl = "";
		JSONObject paintJson = new JSONObject();
		JSONArray jarr = new JSONArray();

		try {
			while (i < 5) {
//				System.out.println(i);
				JSONObject paint = new JSONObject();
				doc = Jsoup.connect(url).get();
				Elements paintingDetails = doc
						.select("html body #wrapper div #mainbody div.elementsview #singledata div div");
				Elements nextButton = doc
						.select("html body #wrapper div #mainbody div div div.pagenavright span a");
				Elements authDetails = doc
						.select("html body #wrapper div #mainbody div.elementsview #singledata");

				// get the next item link
				for (Element e : nextButton) {
					if (e.text().equalsIgnoreCase("Next")) {
						nextUrl = e.attr("abs:href");
					}
				}

				// get the image url
				Elements imgUrl = doc
						.select("html body #wrapper div #mainbody div.elementsview #viewForm #singlemedia div p a");
				img = imgUrl.attr("abs:href");

				// get Painting Name + Author details
				for (Element a : authDetails) {
					title = a.select("strong em").text();
					authb = a.text();
				}

				if (!authb.contains("Date:") || !authb.contains("Dimensions:")
						|| !authb.contains("Medium:")
						|| !authb.contains("Credit Line:")
						|| !authb.contains("Object Number:")) {
					i++;
					url = nextUrl;
					continue;
				}
				authb = StringUtils.substringBetween(authb, "Artist: ",
						"Dimensions:");
				// System.out.println(title);
				// System.out.println(authb);
				// System.out.println(img);

				// get other current painting details
				int j = 0;
				for (Element d : paintingDetails) {
					if (j == 0)
						date = d.text();
					if (j == 1)
						dim = d.text();
					if (j == 2)
						med = d.text();
					if (j == 3)
						obj = d.text();
					if (j == 4)
						cl = d.text();
					// System.out.println(d.text() + "\n");
					j++;
				}
				date = date.substring(date.indexOf(":") + 2);
				// check if painting is after year 1700
				String dnum = "";
				char c;
				int yyyy = 0;
				int x = 0;
				for (x = 0; x < date.length(); x++) {
					c = date.charAt(x);
					if (c >= 48 && c <= 57)
						dnum += c;
					if (dnum.length() == 2)
						x = date.length() + 1;
				}
				// System.out.println(dnum);
				yyyy = Integer.parseInt(dnum);
				if (yyyy < 17) {
					i++;
					url = nextUrl;
					continue;
				}
				dim = dim.substring(dim.indexOf(":") + 2);
				med = med.substring(med.indexOf(":") + 2);
				obj = obj.substring(obj.indexOf(":") + 2);
				cl = cl.substring(cl.indexOf(":") + 2);

				// System.out.println(date);
				// System.out.println(dim);
				// System.out.println(med);
				// System.out.println(obj);
				// System.out.println(cl);

				// create the JSON dataset
				paint.put("title", title);
				paint.put("artist", authb);
				paint.put("date", date);
				paint.put("imgurl", img);
				paint.put("dimensions", dim);
				paint.put("medium", med);
				paint.put("credit", cl);
				paint.put("objnum", obj);

				jarr.add(paint);
				paintJson.put("paintings", jarr);

				url = nextUrl;
				i++;

			}

		} catch (IOException e) {
			System.out.println("An Exception Occurred : " + e);

		}
		FileWriter file = new FileWriter("data/dataset.json"); // write json to
																// file
		file.write(paintJson.toJSONString());
		file.flush();
		file.close();

		// pretty print json using gson library
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jsonOutput = gson.toJson(paintJson);
		System.out.println(jsonOutput);

	}
}
