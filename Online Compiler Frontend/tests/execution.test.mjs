import test from 'node:test';
import assert from 'node:assert/strict';
import { executionLanguage, createExecution } from '../src/execution.js';

const languages = [
  {name:'Java',extension:'java',version:'21.0.1'},
  {name:'Python',extension:'py',version:'3.12.0'},
];

test('Java file routes to Java and uses backend version', () => {
  assert.deepEqual(executionLanguage('.java','Java',languages), {language:'java',version:'21.0.1',filename:'Main.java'});
});
test('Python file routes to Python even if old selected tab says Java', () => {
  assert.deepEqual(executionLanguage('py','Java',languages), {language:'python',version:'3.12.0',filename:'main.py'});
});
test('C++ uses supplied example fallback version', () => {
  assert.equal(executionLanguage('cpp','C++',languages).language, 'cpp');
});
test('Text files are not executed as Java', () => {
  assert.equal(executionLanguage('txt','Java',languages), null);
});
test('START, INPUT and CANCEL follow supplied WebSocket protocol', () => {
  class MockWebSocket {
    static OPEN=1;
    static CONNECTING=0;
    constructor(url){ this.url=url; this.readyState=0; this.sent=[]; MockWebSocket.last=this; }
    send(message){ this.sent.push(JSON.parse(message)); }
    close(){ this.readyState=3; this.onclose?.({code:1000}); }
    open(){ this.readyState=1; this.onopen?.(); }
    message(msg){ this.onmessage?.({data:JSON.stringify(msg)}); }
  }
  globalThis.WebSocket=MockWebSocket;
  globalThis.window={location:{protocol:'http:',host:'localhost:5173'}};
  const events=[];
  const execution=createExecution({onMessage: msg => events.push(msg)});
  execution.start({language:'java',version:'21.0.1',filename:'Main.java',code:'class Main {}'});
  const ws=MockWebSocket.last;
  assert.equal(ws.url,'ws://localhost:5173/ws/execute');
  ws.open();
  const start = ws.sent[0];
  assert.equal(start.type,'START');
  assert.equal(start.language,'java');
  assert.ok(start.executionId, 'START must include executionId');
  assert.equal(start.code,'class Main {}');
  assert.equal(start.filename,'Main.java');
  assert.equal('version' in start, false, 'display version must not be sent as version');
  ws.message({type:'STATUS',status:'RUNNING',executionId:start.executionId});
  execution.input('42');
  execution.cancel();
  assert.deepEqual(ws.sent[1],{type:'INPUT',executionId:start.executionId,data:'42\n'});
  assert.deepEqual(ws.sent[2],{type:'CANCEL',executionId:start.executionId});
  assert.equal(events[0].status,'RUNNING');
});
