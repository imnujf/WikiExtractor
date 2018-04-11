import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.spreada.utils.chinese.ZHConverter;

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;


public class CategoryFetcher
{
	/**
	 * 抽取维基百科的分类页面 —— by imnujf
	 * @throws WikiApiException 
	 * 
	 * */
	
	static String res_path = "./Data/wiki_Category.txt";
	
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

		// 创建Wikipedia处理对象
		Wikipedia wiki = null;
		
		try
		{
			wiki = new Wikipedia(dbConfig);
			
			Integer n = 0;
			String wiki_category_title_trad = "";
			String wiki_category_title_simp = "";
			String wiki_url_prefix = "https://zh.wikipedia.org/wiki/Category:";
			Set<Long> v_id = new HashSet<Long>();  
			
			File file = new File(res_path);
			BufferedWriter fw = null;
			try
			{
				fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
			} catch (UnsupportedEncodingException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (FileNotFoundException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			
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
			try
			{
				fw.close();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.print(n);
		} 
		catch (WikiInitializationException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
