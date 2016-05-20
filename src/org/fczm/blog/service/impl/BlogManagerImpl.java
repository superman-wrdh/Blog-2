package org.fczm.blog.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.directwebremoting.WebContextFactory;
import org.fczm.blog.bean.BlogBean;
import org.fczm.blog.domain.Blog;
import org.fczm.blog.domain.Comment;
import org.fczm.blog.domain.Type;
import org.fczm.blog.service.BlogManager;
import org.fczm.blog.service.util.ManagerTemplate;
import org.fczm.blog.servlet.PhotoServlet;
import org.fczm.common.util.DateTool;
import org.fczm.common.util.FileTool;
import org.fczm.common.util.MengularDocument;

public class BlogManagerImpl extends ManagerTemplate implements BlogManager {
	private String blogOutputFolder;
	private int blogOutputFolderDepth;
	
	public void setBlogOutputFolder(String blogOutputFolder) {
		this.blogOutputFolder = blogOutputFolder;
	}

	public void setBlogOutputFolderDepth(int blogOutputFolderDepth) {
		this.blogOutputFolderDepth = blogOutputFolderDepth;
	}

	@Override
	public String addBlog(String title, String content, String date, String tid) {
		Blog blog=new Blog();
		blog.setTitle(title);
		blog.setContent(content);
		blog.setDate(DateTool.transferDate(date, DateTool.DATE_HOUR_MINUTE_FORMAT));
		blog.setReaders(0);
		Type type=typeDao.get(tid);
		blog.setType(type);
		String bid= blogDao.save(blog);
		if(bid!=null) {
			//根据模板生成文件
			generateBlog(blog);
			//type文章数量加1
			type.setCount(type.getCount()+1);
			typeDao.update(type);
		}
		return bid;
	}

	@Override
	public List<BlogBean> getAll() {
		List<BlogBean> blogs=new ArrayList<>();
		for(Blog blog: blogDao.findAll()) {
			blogs.add(new BlogBean(blog));
		}
		return blogs;
	}

	@Override
	public BlogBean getBlogInfo(String bid, boolean reader) {
		Blog blog=blogDao.get(bid);
		if(blog==null) {
			return null;
		}
		if(reader) {
			blog.setReaders(blog.getReaders()+1);
			blogDao.update(blog);
		}
		return new BlogBean(blog);
	}
	

	@Override
	public String getBlogContent(String bid) {
		Blog blog=blogDao.get(bid);
		if(blog==null)
			return null;
		return blog.getContent();
	}


	@Override
	public void modifyBlog(String bid, String title, String content, String date, String tid) {
		Blog blog=blogDao.get(bid);
		blog.setTitle(title);
		blog.setContent(content);
		blog.setDate(DateTool.transferDate(date, DateTool.DATE_HOUR_MINUTE_FORMAT));
		Type oldType=blog.getType();
		Type newType=typeDao.get(tid);
		blog.setType(newType);
		blogDao.update(blog);
		//更新新旧分类的博文数量
		oldType.setCount(oldType.getCount()-1);
		typeDao.update(oldType);
		newType.setCount(newType.getCount()+1);
		typeDao.update(newType);
		//根据模板生成文件
		generateBlog(blog);
	}

	@Override
	public void backgroudSaving(String bid, String content) {
		Blog blog=blogDao.get(bid);
		blog.setContent(content);
		blogDao.update(blog);
	}

	@Override
	public void removeBlog(String bid) {
		Blog blog=blogDao.get(bid);
		//博文分类数量减1
		Type type=blog.getType();
		type.setCount(type.getCount()-1);
		typeDao.update(type);
		//删除评论
		for(Comment comment: commentDao.findByBlog(blog)) {
			commentDao.delete(comment);
		}
		blogDao.delete(blog);
	}

	@Override
	public int getBlogsCount(String title, String tid) {
		Type type= (tid==null)? null: typeDao.get(tid);
		return blogDao.getBlogsCount(title, type);
	}

	@Override
	public List<BlogBean> searchBlogs(String title, String tid, int page, int pageSize) {
		int offset=(page-1)*pageSize;
		List<BlogBean> blogs=new ArrayList<>();
		Type type= (tid==null)? null: typeDao.get(tid);
		for(Blog blog: blogDao.findByTitle(title, type, offset, pageSize)) {
			blogs.add(new BlogBean(blog));
		}
		return blogs;
	}

	@Override
	public void regenerate() {
		String rootPath=WebContextFactory.get().getServletContext().getRealPath("/")+File.separator;
		FileTool.delAllFile(rootPath+blogOutputFolder);
		for(Blog blog: blogDao.findAll()) {
			generateBlog(blog);
		}
	}
	
	private void generateBlog(Blog blog) {
		String rootPath=WebContextFactory.get().getServletContext().getRealPath("/")+File.separator;
		MengularDocument document=new MengularDocument(rootPath, blogOutputFolderDepth,"blog.html");
		document.setValue("blog-date", DateTool.formatDate(blog.getDate(), DateTool.DATE_HOUR_MINUTE_FORMAT));
		document.setValue("blog-title", blog.getTitle());
		document.setValue("blog-tname", blog.getType().getTname());
		document.setValue("blog-content", blog.getContent());
		document.output(blogOutputFolder+blog.getBid());
	}

	@Override
	public boolean deleteCover(String bid) {
		Blog blog=blogDao.get(bid);
		String rootPath=WebContextFactory.get().getServletContext().getRealPath("/")+File.separator;
		if(new File(rootPath+File.separator+PhotoServlet.COVER_FOLDER+File.separator+blog.getCover()).delete()) {
			blog.setCover(null);
			blogDao.update(blog);
			return true;
		}
		return false;
	}

}