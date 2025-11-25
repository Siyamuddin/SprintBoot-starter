package com.siyamuddin.blog.blogappapis.Config;

/**
 * Application constants for pagination and sorting defaults.
 * Note: Role IDs are now configurable via RoleProperties.
 * 
 * @deprecated Role constants (NORMAL_USER, ADMIN_USER) are deprecated.
 * Use RoleProperties instead for configurable role IDs.
 */
public class AppConstants {
    public static final String PAGE_NUMBER = "0";
    public static final String PAGE_SIZE = "5";
    public static final String SORT_BY = "postId";
    public static final String SORT_DIREC = "asc";
    
    /**
     * @deprecated Use RoleProperties.getNormalUser() instead
     */
    @Deprecated
    public static final Integer NORMAL_USER = 2;
    
    /**
     * @deprecated Use RoleProperties.getAdminUser() instead
     */
    @Deprecated
    public static final Integer ADMIN_USER = 1;
}
