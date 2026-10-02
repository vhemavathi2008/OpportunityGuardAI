<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Settings"/><c:set var="active" value="settings"/>
<%@ include file="_header.jspf" %>

<div class="grid g2" style="align-items:start">
  <section class="card">
    <h2>Change password</h2>
    <form method="post" action="${ctx}/settings">
      <input type="hidden" name="_csrf" value="${csrfToken}">
      <div class="field"><label for="currentPassword">Current password</label>
        <input id="currentPassword" type="password" name="currentPassword" autocomplete="current-password" required></div>
      <div class="field"><label for="newPassword">New password</label>
        <input id="newPassword" type="password" name="newPassword" autocomplete="new-password" required></div>
      <div class="field"><label for="confirmPassword">Confirm new password</label>
        <input id="confirmPassword" type="password" name="confirmPassword" autocomplete="new-password" required></div>
      <button class="btn" type="submit">Update password</button>
    </form>
  </section>
  <section class="card">
    <h2>Integrations</h2>
    <dl class="kv">
      <dt>Google OAuth</dt><dd><span class="badge ${oauthConfigured ? 't-good' : 't-unknown'}">${oauthConfigured ? 'Configured on server' : 'Not configured'}</span></dd>
      <dt>Gemini API</dt><dd><span class="badge ${geminiConfigured ? 't-good' : 't-unknown'}">${geminiConfigured ? 'Configured on server' : 'Not configured'}</span></dd>
      <dt>Your Gmail</dt><dd><span class="badge ${gmailConnected ? 't-good' : 't-unknown'}">${gmailConnected ? 'Connected' : 'Not connected'}</span></dd>
    </dl>
    <p style="margin-top:16px"><a class="btn ghost" href="${ctx}/gmail">Manage Gmail</a></p>
  </section>
</div>
<%@ include file="_footer.jspf" %>
