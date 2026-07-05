function openDialog(id) {
  var el = document.getElementById(id);
  if (el) el.classList.add('show');
}

function closeDialog(id) {
  var el = document.getElementById(id);
  if (el) el.classList.remove('show');
}

function setMode(btn, labelId, text) {
  var group = btn.parentNode;
  Array.prototype.forEach.call(group.children, function (item) {
    item.classList.remove('active');
  });
  btn.classList.add('active');
  var label = document.getElementById(labelId);
  if (label) label.innerText = text;
}

function selectModeAndConfirm(btn, labelId, text, dialogId) {
  setMode(btn, labelId, text);
  openDialog(dialogId);
}

function selectSummaryMode(btn, labelId, text, dialogId, selectedInputId, clearInputId) {
  setMode(btn, labelId, text);
  var selectedInput = document.getElementById(selectedInputId);
  var clearInput = document.getElementById(clearInputId);
  if (selectedInput) selectedInput.disabled = false;
  if (clearInput) {
    clearInput.value = '';
    clearInput.disabled = true;
  }
  openDialog(dialogId);
}

function confirmMode(dialogId, lockedTextId, modeTextId) {
  var mode = document.getElementById(modeTextId);
  var locked = document.getElementById(lockedTextId);
  if (locked && mode) {
    locked.innerText = '已确认：' + mode.innerText + '，后续按月生成服务费汇总，不能修改';
  }
  closeDialog(dialogId);
}

function openSummaryIfMode(modeTextId, dialogId) {
  var mode = document.getElementById(modeTextId);
  if (!mode || !mode.innerText || mode.innerText === '未选择') {
    alert('请先选择服务费汇总方式');
    return;
  }
  if (window.__summaryValidateFail) {
    alert('所选结算单不满足生成条件：存在已生成服务费汇总的结算单，请重新选择。');
    return;
  }
  openDialog(dialogId);
}

function toast(text) {
  alert(text);
}
