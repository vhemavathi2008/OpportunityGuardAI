<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="o" required="true" type="com.opportunityguard.model.Opportunity" %>
<c:choose>
  <c:when test="${empty o.deadline}"><span class="muted">No deadline</span></c:when>
  <c:otherwise>
    <c:out value="${o.deadline}"/>
    <span class="dl ${o.deadlineClass}">
      <c:choose>
        <c:when test="${o.daysLeft lt 0}">overdue</c:when>
        <c:when test="${o.daysLeft == 0}">today</c:when>
        <c:when test="${o.daysLeft == 1}">tomorrow</c:when>
        <c:otherwise>${o.daysLeft} days left</c:otherwise>
      </c:choose>
    </span>
  </c:otherwise>
</c:choose>
