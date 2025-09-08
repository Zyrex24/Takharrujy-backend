package com.university.takharrujy.domain.enums;

/**
 * User Activity Types
 * Enumeration of all possible user activity types in the system
 */
public enum UserActivityType {
    
    // Authentication activities
    LOGIN("تسجيل الدخول"),
    LOGOUT("تسجيل الخروج"),
    PASSWORD_CHANGE("تغيير كلمة المرور"),
    PROFILE_UPDATE("تحديث الملف الشخصي"),
    EMAIL_VERIFICATION("تأكيد البريد الإلكتروني"),
    
    // Project activities
    PROJECT_CREATE("إنشاء مشروع"),
    PROJECT_UPDATE("تحديث مشروع"),
    PROJECT_DELETE("حذف مشروع"),
    PROJECT_JOIN("انضمام لمشروع"),
    PROJECT_LEAVE("مغادرة مشروع"),
    PROJECT_INVITE_SENT("إرسال دعوة مشروع"),
    PROJECT_INVITE_ACCEPTED("قبول دعوة مشروع"),
    PROJECT_INVITE_REJECTED("رفض دعوة مشروع"),
    
    // Task activities
    TASK_CREATE("إنشاء مهمة"),
    TASK_UPDATE("تحديث مهمة"),
    TASK_DELETE("حذف مهمة"),
    TASK_ASSIGN("تعيين مهمة"),
    TASK_COMPLETE("إكمال مهمة"),
    TASK_REOPEN("إعادة فتح مهمة"),
    
    // File activities
    FILE_UPLOAD("رفع ملف"),
    FILE_DOWNLOAD("تحميل ملف"),
    FILE_DELETE("حذف ملف"),
    FILE_SHARE("مشاركة ملف"),
    
    // Communication activities
    MESSAGE_SEND("إرسال رسالة"),
    MESSAGE_READ("قراءة رسالة"),
    NOTIFICATION_READ("قراءة إشعار"),
    
    // System activities
    PREFERENCES_UPDATE("تحديث التفضيلات"),
    AVATAR_UPLOAD("رفع صورة شخصية"),
    SETTINGS_CHANGE("تغيير الإعدادات");
    
    private final String arabicDescription;
    
    UserActivityType(String arabicDescription) {
        this.arabicDescription = arabicDescription;
    }
    
    public String getArabicDescription() {
        return arabicDescription;
    }
    
    public String getDescription() {
        return name().toLowerCase().replace('_', ' ');
    }
}