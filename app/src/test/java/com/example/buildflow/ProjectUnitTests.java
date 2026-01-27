package com.example.buildflow;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import java.util.List;
import java.util.ArrayList;

import com.example.buildflow.model.ProjectRequest;

/**
 * מטלה 4 - חלק B: כתיבת Unit Tests
 * כל הבדיקות מתמקדות בתהליך ניהול התקלות (Request Management)
 */
public class ProjectUnitTests {

    // --- בדיקה 1: יצירת תקלה חדשה ---
    // בודקים שהבנאי (Constructor) שומר את כל הנתונים הקריטיים כמו שצריך.
    @Test
    public void testNewRequestCreation() {
        // 1. Setup
        String title = "Broken Pipe";
        String urgency = "High";
        String location = "Floor 2";

        // 2. Action
        ProjectRequest request = new ProjectRequest(
                title, "Plumbing", "Leak description", urgency, location,
                "01/01/2026", "10:00", "uid123", null
        );

        // 3. Assertion (אימות)
        assertEquals("Title should match", title, request.getTitle());
        assertEquals("Urgency should match", urgency, request.getUrgency());
        assertEquals("Location should match", location, request.getLocation());
    }

    // --- בדיקה 2: שינוי סטטוס תקלה ---
    // בודקים את הלוגיקה של עדכון סטטוס (למשל, מעבר מ"פתוח" ל"בטיפול")
    @Test
    public void testRequestStatusUpdate() {
        // 1. Setup - יצירת תקלה עם סטטוס ברירת מחדל (נניח "Open")
        ProjectRequest request = new ProjectRequest();
        request.setStatus("Open");

        // 2. Action - המנהל משנה סטטוס ל"In Progress"
        request.setStatus("In Progress");

        // 3. Assertion - וידוא שהשינוי נשמר
        assertEquals("Status should be updated to In Progress", "In Progress", request.getStatus());
    }

    // --- בדיקה 3: שימוש ב-Mock Objects (חובה!) ---
    // כאן נדמה תרחיש שבו רשימת התקלות של הפרויקט מתעדכנת.
    // אנחנו יוצרים "חיקוי" של הרשימה כדי לוודא שהקוד באמת מנסה להוסיף אליה את התקלה.
    @Test
    public void testAddRequestToProjectListWithMock() {
        // 1. יצירת Mock של רשימת בקשות (במקום רשימה אמיתית)
        List<ProjectRequest> mockedRequestList = Mockito.mock(List.class);

        // יצירת אובייקט תקלה לבדיקה
        ProjectRequest newRequest = new ProjectRequest("Fix Light", "Electrical", "...", "Low", "Lobby", "date", "time", "uid", null);

        // 2. Action - הוספת התקלה לרשימה המדומה
        mockedRequestList.add(newRequest);

        // 3. Verify - בדיקה האם הפונקציה add באמת נקראה עם התקלה הספציפית הזו?
        Mockito.verify(mockedRequestList).add(newRequest);

        // (אופציונלי) וידוא שלא קרו דברים מוזרים אחרים, כמו מחיקת הרשימה
        Mockito.verify(mockedRequestList, Mockito.never()).clear();
    }
}