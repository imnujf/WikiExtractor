package cn.imnujf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.spreada.utils.chinese.ZHConverter;

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Template;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;



public class Extractor
{
	/**
	 * 抽取维基百科的分类页面 —— by imnujf
	 * @throws WikiApiException 
	 * 
	 * */
	
	static String category_res_path = "./Data/wiki_Category.txt";
	static String content_res_path = "./Data/wiki_Content.txt";
	static String wiki_content_file_path = "./Data/wiki_Content.csv";
	static String wiki_id2category_file_path = "./Data/wiki_id2Category.csv";
	static String NEW_LINE_SEPARATOR="\n";
	
	// 获取infobox
	public static ArrayList<String> FetchInfobox(Page page)
	{
		ArrayList<String> infobox = new ArrayList<String>();
		MediaWikiParserFactory pf = new MediaWikiParserFactory();
		MediaWikiParser parser = pf.createParser();
		ParsedPage pp = parser.parse(page.getText());

		// 获取infobox
		int len = 0;
		String[] ib = new String[10000];

		for (Template t : pp.getTemplates())
		{
			if (t.getName().toLowerCase().startsWith("infobox"))
			{
				for (String tp : t.getParameters())
				{
					ib[len] = tp;
					len += 1;
				}
			}
		}
		int j = 0;
		String[] ib_rs = new String[10000];

		for (int i = 0; i < len; i++)
		{
			ib_rs[j] = ib[i];

			if (i + 1 < len && ib[i + 1].indexOf('=') == -1)
			{
				ib_rs[j] += ib[i + 1];
				i += 1;
			}
			j++;
		}

		for (int i = 0; i < j; i++)
		{
			String[] xx = ib_rs[i].split("=");
			if (xx.length > 1)
			{
				System.out.println(xx[0].trim() + "\t" + xx[1].trim());
			}

		}
		return infobox;
	}
	
	/**
	 * 建立实体和类别的映射关系
	 */
	public static ArrayList<String> Id2Category(int id, Set<Category> category_set) throws WikiTitleParsingException
	{
		ArrayList<String> res = new ArrayList<String>();
		
		for (Category category_page : category_set) 
		{  
		      System.out.println(id+"\t所属类别\t"+category_page.__getId());  
		      res.add(id+"\t所属类别\t"+category_page.__getId());
		}  
		return res;
	}
	
	public static void CategoryExtractor(DatabaseConfiguration db_config, ZHConverter converter)
	{
		try
		{
			Wikipedia wiki = new Wikipedia(db_config);
			
			Integer n = 0;
			String wiki_category_title_trad = "";
			String wiki_category_title_simp = "";
			String wiki_url_prefix = "https://zh.wikipedia.org/wiki/Category:";
			Set<Long> v_id = new HashSet<Long>();  
			
			File file = new File(category_res_path);
			BufferedWriter fw = null;
			fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));

			
			for (Category it_category : wiki.getCategories()) 
			{
				try
				{
					wiki_category_title_trad = it_category.getTitle().toString();
					wiki_category_title_simp = converter.convert(wiki_category_title_trad);
					long wiki_category_id = it_category.__getId();
					
					if(!v_id.contains(wiki_category_id))
					{
						v_id.add(wiki_category_id);
						
						Set<Category> sub_category = it_category.getChildren();
					
						for (Category x : sub_category)
						{
							try
							{
								long son_id = x.__getId();
								
								//# 
								fw.write(wiki_category_id+"\t"+wiki_url_prefix+wiki_category_title_trad+"\t"+wiki_category_title_simp
										+ "\t"+son_id+"\t"+wiki_url_prefix+x.getTitle().toString()+"\t"+converter.convert(x.getTitle().toString())+"\n");
								
							} catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						try
						{
							fw.flush();
						} catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} 
				catch (WikiTitleParsingException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				n++;
				
				if (n%1000 == 0)
				{
					System.out.print(n);
					Date d = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					System.out.println("当前时间：" + sdf.format(d));
				}
			    
			}
			fw.close();
			System.out.print(n);
		} 
		catch (WikiInitializationException | IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	public static void ContentExtractor(DatabaseConfiguration db_config, ZHConverter converter, String data_type) throws WikiInitializationException, IOException
	{

		Wikipedia wiki = new Wikipedia(db_config);
		
		Integer n = 0;
		String wiki_url_prefix = "https://zh.wikipedia.org/wiki/";	//维基百科，地址前缀
		CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR); //初始化csvformat
		String file_path = "";
		String[] hders = new String[]{};
		
		if(data_type == "content")
		{
			file_path = wiki_content_file_path;
			hders = new String[]{"id", "title_simple", "url", "wiki_plain_text"};
		}
		
		if (data_type == "i2ci")
		{
			file_path = wiki_id2category_file_path;
			hders = new String[]{"id", "rel", "category_id"};
		}
	    
		FileWriter fileWriter=new FileWriter(file_path); 
	    CSVPrinter printer =new CSVPrinter(fileWriter, formator); 
	    printer.printRecord(hders);
		
		for (Page it_page : wiki.getPages()) 
		{
			try
			{
				int wiki_id = it_page.getPageId();
				String wiki_content_title_trad = it_page.getTitle().toString();
				System.out.println(wiki_content_title_trad);
				String wiki_content_title_simp = converter.convert(wiki_content_title_trad);
				String wiki_url = wiki_url_prefix + wiki_content_title_trad;
				String wiki_plain_text = it_page.getPlainText();
				
				if (data_type == "content")
				{
					printer.print(String.valueOf(wiki_id));
					printer.print(wiki_content_title_simp);
					printer.print(wiki_url);
					printer.print(wiki_plain_text.replaceAll("\n", "@@@@@@@"));
					printer.println();
					printer.flush();
				}
				// ArrayList<String> info_box = FetchInfobox(it_page);
				// String wiki_keys = info_box.get(0);
				// String wiki_values = info_box.get(1);
				
				// wiki id to category id info
				
				if (data_type == "i2ci")
				{
					ArrayList<String> wiki_categories = Id2Category(wiki_id, it_page.getCategories());
					for (String s: wiki_categories)
					{
						printer.print(s);
					}
				}
			} 
			catch (WikiApiException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			n++;
			
			if (n%1000 == 0)
			{
				System.out.print(n);
				Date d = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				System.out.println("当前时间：" + sdf.format(d));
			}
		    
		}

	}
	

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		
		// 数据库连接参数配置
		DatabaseConfiguration dbConfig = new DatabaseConfiguration();
		dbConfig.setHost("192.168.126.183"); // 主机名
		dbConfig.setDatabase("JWPL"); // 数据库名
		dbConfig.setUser("root"); // 访问数据库的用户名
		dbConfig.setPassword("123456"); // 访问数据库的密码
		dbConfig.setLanguage(Language.chinese);
		
		// 创建繁简转换工具, 顺便测试一下
		ZHConverter converter = ZHConverter.getInstance(ZHConverter.SIMPLIFIED);
		String simplifiedStr = converter.convert("有背光的機械式鍵盤");
		System.out.println(simplifiedStr);
		
		//CategoryExtractor(dbConfig, converter);
		
		// data_type 可以分为三种: content:正文， category:类别信息， infobox:infobox信息
		String data_type = "content";
		try
		{
			ContentExtractor(dbConfig, converter, data_type);
			
		} 
		catch (WikiInitializationException | IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
