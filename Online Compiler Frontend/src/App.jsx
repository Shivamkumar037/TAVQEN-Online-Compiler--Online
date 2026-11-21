import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import Editor from '@monaco-editor/react';
import {
  Activity, Bell, Bot, Check, ChevronDown, ChevronRight, CircleHelp, Code2,
  Copy, File, FileCode2, FilePlus2, Folder, FolderPlus, FolderOpen, GitBranch,
  LockKeyhole, LogIn, Menu, MessageSquare, Moon, MoreHorizontal, PanelLeftClose,
  Play, Plus, Save, Send, Settings, Sparkles, Sun, Trash2, X, Zap, Maximize2, Minimize2,
  Palette, Monitor,
} from 'lucide-react';
import { api } from './api';
import { createExecution, executionLanguage } from './execution';
import { normalizeStoredCode } from './code-content';

const STARTERS = {
  java: { name: 'Main', ext: 'java', code: 'public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello, TAVQEN!");\n    }\n}' },
  python: { name: 'main', ext: 'py', code: 'print("Hello, TAVQEN!")' },
  c: { name: 'main', ext: 'c', code: '#include <stdio.h>\n\nint main() {\n    printf("Hello, TAVQEN!\\n");\n    return 0;\n}' },
  cpp: { name: 'main', ext: 'cpp', code: '#include <iostream>\nusing namespace std;\n\nint main() {\n    cout << "Hello, TAVQEN!" << endl;\n    return 0;\n}' },
  javascript: { name: 'index', ext: 'js', code: 'console.log("Hello, TAVQEN!");' },
  typescript: { name: 'index', ext: 'ts', code: 'const message: string = "Hello, TAVQEN!";\nconsole.log(message);' },
  go: { name: 'main', ext: 'go', code: 'package main\n\nimport "fmt"\n\nfunc main() {\n    fmt.Println("Hello, TAVQEN!")\n}' },
  rust: { name: 'main', ext: 'rs', code: 'fn main() {\n    println!("Hello, TAVQEN!");\n}' },
  php: { name: 'index', ext: 'php', code: '<?php\necho "Hello, TAVQEN!";\n?>' },
  html: { name: 'index', ext: 'html', code: '<!doctype html>\n<html>\n<head><title>Hello</title></head>\n<body><h1>Hello, TAVQEN!</h1></body>\n</html>' },
  sql: { name: 'query', ext: 'sql', code: 'SELECT "Hello, TAVQEN!" AS message;' },
  ruby: { name: 'main', ext: 'rb', code: 'puts "Hello, TAVQEN!"' },
  kotlin: { name: 'Main', ext: 'kt', code: 'fun main() {\n    println("Hello, TAVQEN!")\n}' },
  swift: { name: 'main', ext: 'swift', code: 'print("Hello, TAVQEN!")' },
  csharp: { name: 'Program', ext: 'cs', code: 'using System;\nclass Program {\n    static void Main() { Console.WriteLine("Hello, TAVQEN!"); }\n}' },
};
const FALLBACK_LANGS = [
  ['Java','java'], ['Python','python'], ['C','c'], ['C++','cpp'], ['JavaScript','javascript'],
  ['TypeScript','typescript'], ['Go','go'], ['Rust','rust'], ['PHP','php'], ['HTML','html'], ['SQL','sql']
].map(([name, key], i) => ({ id: i + 1, name, extension: STARTERS[key].ext, icon: '', version: '', key }));
const extToLanguage = { java:'java', py:'python', c:'c', cpp:'cpp', js:'javascript', ts:'typescript', go:'go', rs:'rust', php:'php', html:'html', sql:'sql', txt:'plaintext', css:'css', rb:'ruby', kt:'kotlin', swift:'swift', cs:'csharp' };
const languageKey = (name = '') => name.toLowerCase().replace('c++','cpp').replace('c#','csharp').replace(/[^a-z]/g,'');
const starterFor = (lang) => STARTERS[languageKey(lang)] || STARTERS.java;
const currentFileName = (file) => `${file.fileName}.${(file.fileExtension || '').replace(/^\./, '')}`;
const AI_MODES = [
  { value: 1, label: 'Ask Anything', description: 'General coding help' },
  { value: 2, label: 'DSA', description: 'Data structures and algorithms' },
  { value: 3, label: 'Code Generator', description: 'Generate code from a prompt' },
  { value: 4, label: 'Error Fixer', description: 'Find and fix code errors' },
  { value: 5, label: 'Dry Run', description: 'Trace code step by step' },
  { value: 6, label: 'Code Analyze', description: 'Review the current code' },
];

function Modal({ title, onClose, children }) {
  return <div className="modal-backdrop" onMouseDown={onClose}><div className="modal" onMouseDown={e => e.stopPropagation()}><div className="modal-head"><strong>{title}</strong><button className="icon-btn" onClick={onClose}><X size={17}/></button></div>{children}</div></div>;
}
function TreeNode({ node, depth = 0, onOpenFile, onCreateFolder, onCreateFile, onRename, onDelete, onExpandFolder, onSelectFolder, selectedFolderId, activeFileId }) {
  const [expanded, setExpanded] = useState(depth < 1);
  const isFolder = Array.isArray(node.folders) || node.kind === 'folder';
  const [menu, setMenu] = useState(false);
  return <div className="tree-node">
    <div className={`tree-row ${isFolder && selectedFolderId === node.id ? 'selected' : ''} ${!isFolder && activeFileId === node.id ? 'selected' : ''}`} style={{ paddingLeft: 9 + depth * 13 }} onClick={() => { if (isFolder) { onSelectFolder?.(node); const next = !expanded; setExpanded(next); if (next && !node.loaded) onExpandFolder?.(node.id); } else onOpenFile(node); }}>
      {isFolder ? (expanded ? <ChevronDown size={13}/> : <ChevronRight size={13}/>) : <span className="tree-spacer"/>}
      {isFolder ? (expanded ? <FolderOpen size={14} className="folder-icon"/> : <Folder size={14} className="folder-icon"/>) : <FileCode2 size={14} className="file-icon"/>}
      <span className="tree-label">{node.folderName || `${node.fileName}${node.fileExtension ? `.${node.fileExtension.replace(/^\./,'')}` : ''}`}</span>
      <button className="tree-more" onClick={e => {e.stopPropagation(); setMenu(v => !v)}}><MoreHorizontal size={14}/></button>
      {menu && <div className="context-menu" onClick={e => e.stopPropagation()}>
        {isFolder && <><button onClick={() => {setMenu(false); onCreateFolder(node)}}><FolderPlus size={13}/> New folder</button><button onClick={() => {setMenu(false); onCreateFile(node)}}><FilePlus2 size={13}/> New file</button></>}
        <button onClick={() => {setMenu(false); onRename(node, isFolder ? 'folder' : 'file')}}>Rename</button>
        <button className="danger-text" onClick={() => {setMenu(false); onDelete(node, isFolder ? 'folder' : 'file')}}><Trash2 size={13}/> Delete</button>
      </div>}
    </div>
    {isFolder && expanded && <div>{(node.folders || []).map(f => <TreeNode key={`d${f.id}`} node={{...f, kind:'folder'}} depth={depth+1} {...{onOpenFile,onCreateFolder,onCreateFile,onRename,onDelete,onExpandFolder,onSelectFolder,selectedFolderId,activeFileId}}/>)}{(node.files || []).map(f => <TreeNode key={`f${f.id}`} node={f} depth={depth+1} {...{onOpenFile,onCreateFolder,onCreateFile,onRename,onDelete,onExpandFolder,onSelectFolder,selectedFolderId,activeFileId}}/>)}</div>}
  </div>;
}

export default function App() {
  const [loggedIn, setLoggedIn] = useState(false);
  const [authChecking, setAuthChecking] = useState(true);
  const [preferencesReady, setPreferencesReady] = useState(false);
  const [selectedFolderId, setSelectedFolderId] = useState(null);
  const [openTabs, setOpenTabs] = useState([]);
  const [scratchTabs, setScratchTabs] = useState([]);
  const [htmlPreview, setHtmlPreview] = useState(false);
  const [previewExpanded, setPreviewExpanded] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [theme, setTheme] = useState(() => localStorage.getItem('oc-theme') || 'midnight');
  const [user, setUser] = useState(null);
  const [rootFolder, setRootFolder] = useState(null);
  const [languages, setLanguages] = useState(FALLBACK_LANGS);
  const [selectedLang, setSelectedLang] = useState(() => localStorage.getItem('oc-language') || 'Java');
  const [explorerOpen, setExplorerOpen] = useState(true);
  const [aiOpen, setAiOpen] = useState(true);
  const [mobileDrawer, setMobileDrawer] = useState(false);
  const [mobileAI, setMobileAI] = useState(false);
  const [authMode, setAuthMode] = useState('');
  const [profileOpen, setProfileOpen] = useState(false);
  const [tree, setTree] = useState(null);
  const [activeFile, setActiveFile] = useState(null);
  const [code, setCode] = useState(STARTERS.java.code);
  const [saved, setSaved] = useState(true);
  const [consoleOpen, setConsoleOpen] = useState(false);
  const [consoleTab, setConsoleTab] = useState('Terminal');
  const [consoleMaximized, setConsoleMaximized] = useState(false);
  const [stdin, setStdin] = useState('');
  const [inputRequested, setInputRequested] = useState(false);
  const executionRef = useRef(null);
  const editorRef = useRef(null);
  const editorChangeSubscriptionRef = useRef(null);
  const syncingEditorRef = useRef(false);
  const outputBufferRef = useRef('');
  const [executionStatus, setExecutionStatus] = useState('IDLE');
  const [stderr, setStderr] = useState('');
  const [executionDetails, setExecutionDetails] = useState('');
  const [outputText, setOutputText] = useState('');
  const [consoleMessage, setConsoleMessage] = useState('Run your code to see the output here.');
  const [modal, setModal] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [toast, setToast] = useState('');
  const [chatInput, setChatInput] = useState('');
  const [selectedMode, setSelectedMode] = useState(1);
  const [modelMenuOpen, setModelMenuOpen] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiWidth, setAiWidth] = useState(300);
  const workspaceRef = useRef(null);
  const resizeDragRef = useRef(null);
  const [chat, setChat] = useState([{ role:'assistant', text:'Hi! I’m your AI Coding Assistant. I can help you understand, debug, optimize, and review your code.' }]);
  const desktopChatRef = useRef(null);
  const mobileChatRef = useRef(null);

  // Keep the newest question/answer in view whenever the chat changes.
  useEffect(() => {
    const container = mobileAI ? mobileChatRef.current : desktopChatRef.current;
    if (!container) return;
    requestAnimationFrame(() => {
      container.scrollTo({ top: container.scrollHeight, behavior: 'smooth' });
    });
  }, [chat, mobileAI, aiOpen]);

  useEffect(() => () => executionRef.current?.close(), []);

  // Keep Monaco's model uncontrolled while typing. This avoids React pushing
  // the same value back into Monaco on every keystroke, which can make the
  // caret jump or place inserted text at an unexpected position.
  useEffect(() => {
    const editor = editorRef.current;
    if (!editor) return;
    const currentValue = editor.getValue();
    if (currentValue !== code) {
      syncingEditorRef.current = true;
      editor.setValue(code);
      syncingEditorRef.current = false;
    }
  }, [code]);

  useEffect(() => () => editorChangeSubscriptionRef.current?.dispose(), []);

  const languageOptions = useMemo(() => languages.map(l => ({...l, key: languageKey(l.name)})), [languages]);
  const notify = (message) => { setToast(message); window.clearTimeout(notify.timer); notify.timer = window.setTimeout(() => setToast(''), 2800); };

  useEffect(() => { api.languages().then(data => { if (data?.languages?.length) setLanguages(data.languages.map(l => ({...l, key: languageKey(l.name)}))); }).catch(() => {}); }, []);

  // Restore the authenticated session from the HttpOnly cookie after refresh.
  // localStorage stores only UI preferences and workspace pointers, never the JWT.
  useEffect(() => {
    let cancelled = false;
    const restore = async () => {
      let storedFileId = localStorage.getItem('oc-active-file-id');
      let storedTabIds = [];
      try { storedTabIds = JSON.parse(localStorage.getItem('oc-open-tab-ids') || '[]'); }
      catch { localStorage.removeItem('oc-open-tab-ids'); }
      try {
        const me = await api.me();
        if (cancelled || !me?.userFolder?.id) return;
        setUser(me);
        setRootFolder(me.userFolder);
        setLoggedIn(true);
        setSelectedFolderId(me.userFolder.id);
        await loadFolder(me.userFolder.id);
        if (Array.isArray(storedTabIds) && storedTabIds.length) {
          const restored = await Promise.all(storedTabIds.map(async id => {
            try { return await api.getFile(id); } catch { return null; }
          }));
          if (!cancelled) setOpenTabs(restored.filter(Boolean));
        }
        if (storedFileId) {
          try {
            const file = await api.getFile(storedFileId);
            if (!cancelled) {
              setActiveFile(file);
              const savedDraft = localStorage.getItem(`oc-draft-${storedFileId}`);
              setCode(savedDraft ?? normalizeStoredCode(file.fileContent || ''));
              const ext = (file.fileExtension || '').replace(/^\./, '').toLowerCase();
              const lang = languages.find(l => (l.extension || '').replace(/^\./, '').toLowerCase() === ext);
              if (lang) setSelectedLang(lang.name);
            }
          } catch { localStorage.removeItem('oc-active-file-id'); }
        }
      } catch {
        // A 401 means there is no valid login cookie. Keep the guest workspace.
      } finally {
        if (!cancelled) {
          setAuthChecking(false);
          setPreferencesReady(true);
        }
      }
    };
    restore();
    return () => { cancelled = true; };
  }, []);

  useEffect(() => { localStorage.setItem('oc-theme', theme); }, [theme]);
  useEffect(() => { localStorage.setItem('oc-language', selectedLang); }, [selectedLang]);
  useEffect(() => {
    const draftKey = activeFile?.id ? `oc-draft-${activeFile.id}` : 'oc-draft-scratch';
    localStorage.setItem(draftKey, code);
  }, [code, activeFile?.id]);
  useEffect(() => {
    if (!activeFile?.scratchKey) return;
    setScratchTabs(tabs => tabs.map(tab => tab.scratchKey === activeFile.scratchKey
      ? { ...tab, fileContent: code }
      : tab));
  }, [code, activeFile?.scratchKey]);
  useEffect(() => {
    if (!preferencesReady) return;
    if (activeFile?.id) localStorage.setItem('oc-active-file-id', String(activeFile.id));
    else localStorage.removeItem('oc-active-file-id');
  }, [activeFile?.id, preferencesReady]);
  useEffect(() => {
    if (!preferencesReady) return;
    localStorage.setItem('oc-open-tab-ids', JSON.stringify(openTabs.map(tab => tab.id)));
  }, [openTabs, preferencesReady]);


  const loadFolder = useCallback(async (folderId) => {
    const data = await api.folderContents(folderId);
    const fresh = { id: data.folderId, folderName: data.folderName, kind:'folder', loaded:true, folders: (data.folders || []).map(f => ({...f, kind:'folder', loaded:false})), files: data.files || [] };
    setTree(current => current?.id === folderId ? {
      ...fresh,
      folders: fresh.folders.map(f => {
        const previous = current.folders?.find(old => old.id === f.id);
        return previous?.loaded ? { ...previous, folderName: f.folderName } : f;
      }),
    } : fresh);
    return data;
  }, []);

  const finishAuth = async (result) => {
    const root = result?.userFolder;
    if (!root?.id) throw new Error('Authentication succeeded, but the response did not include userFolder.id.');
    setUser(result); setRootFolder(root); setLoggedIn(true); setSelectedFolderId(root.id); setAuthMode('');
    await loadFolder(root.id);
    notify('Welcome to your workspace');
  };
  const submitAuth = async (e) => {
    e.preventDefault(); setBusy(true); setError('');
    const form = new FormData(e.currentTarget);
    const body = authMode === 'register'
      ? { name: form.get('name'), email: form.get('email'), password: form.get('password'), gender: form.get('gender') }
      : { email: form.get('email'), password: form.get('password') };
    try { const result = authMode === 'register' ? await api.register(body) : await api.login(body); await finishAuth(result); }
    catch (err) { setError(err.message); }
    finally { setBusy(false); }
  };

  const selectLanguage = (lang) => {
    // Each language owns one temporary editor tab. Selecting it again restores
    // the same unsaved draft; nothing is written to the workspace until Save.
    executionRef.current?.close();
    executionRef.current = null;
    setExecutionStatus('IDLE');
    setInputRequested(false);
    setStdin('');
    setSelectedLang(lang.name);

    const key = languageKey(lang.name);
    const existingScratch = scratchTabs.find(tab => tab.scratchKey === key);
    if (existingScratch) {
      setActiveFile(existingScratch);
      setCode(existingScratch.fileContent || '');
      setSaved(false);
      setConsoleOpen(false);
      setConsoleMaximized(false);
      setHtmlPreview(false);
      return;
    }

    const fallback = starterFor(lang.name);
    const extension = String(lang.extension || fallback.ext).replace(/^\./, '').toLowerCase();
    const defaultCode = typeof lang.defaultCode === 'string' && lang.defaultCode.length
      ? lang.defaultCode
      : fallback.code;
    const defaultName = lang.defaultFileName || ({
      java: 'Main', python: 'app', javascript: 'index', typescript: 'index',
      php: 'index', html: 'index', sql: 'query', c: 'main', cpp: 'main',
      csharp: 'Program', go: 'main', rust: 'main', kotlin: 'Main',
      swift: 'main', ruby: 'main', dart: 'main',
    }[key] || 'main');

    const scratch = {
      tabKey: `scratch:${key}`,
      scratchKey: key,
      fileName: defaultName,
      fileExtension: extension,
      fileContent: defaultCode,
      languageName: lang.name,
      isScratch: true,
    };
    setScratchTabs(tabs => [...tabs, scratch]);
    setActiveFile(scratch);
    setCode(defaultCode);
    setSaved(false);
    setConsoleOpen(false);
    setConsoleMaximized(false);
    setHtmlPreview(false);
    notify(`Opened ${defaultName}.${extension} (temporary)`);
  };

  const activateScratchTab = (tab) => {
    executionRef.current?.close();
    executionRef.current = null;
    setExecutionStatus('IDLE');
    setInputRequested(false);
    setStdin('');
    setSelectedLang(tab.languageName || languages.find(l => languageKey(l.name) === tab.scratchKey)?.name || tab.scratchKey);
    setActiveFile(tab);
    setCode(tab.fileContent || '');
    setSaved(false);
    setConsoleOpen(false);
    setHtmlPreview(false);
  };

  const openFile = async (file) => {
    try { setBusy(true); const data = await api.getFile(file.id); setOpenTabs(tabs => tabs.some(tab => tab.id === data.id) ? tabs : [...tabs, data]); executionRef.current?.close(); executionRef.current = null; setExecutionStatus('IDLE'); setActiveFile(data); setCode(normalizeStoredCode(data.fileContent || '')); setSelectedLang(languages.find(l => l.extension?.replace(/^\./,'').toLowerCase() === (data.fileExtension || '').replace(/^\./,'').toLowerCase())?.name || ({java:'Java',py:'Python',c:'C',cpp:'C++',js:'JavaScript',ts:'TypeScript',go:'Go',rs:'Rust',php:'PHP',html:'HTML',sql:'SQL',txt:'Text'})[(data.fileExtension || '').replace(/^\./,'').toLowerCase()] || 'Unknown'); setSaved(true); setConsoleOpen(false); setMobileDrawer(false); }
    catch (err) { notify(err.message); }
    finally { setBusy(false); }
  };
  const activateTab = async (tab) => { await openFile(tab); };
  const closeTab = async (tab, event) => {
    event?.stopPropagation();
    const nextTabs = openTabs.filter(item => item.id !== tab.id);
    setOpenTabs(nextTabs);
    if (activeFile?.id === tab.id) {
      const fallback = nextTabs[nextTabs.length - 1];
      if (fallback) await openFile(fallback);
      else {
        setActiveFile(null);
        setCode(STARTERS[languageKey(selectedLang)]?.code || STARTERS.java.code);
        localStorage.removeItem('oc-active-file-id');
        setSaved(true);
      }
    }
  };
  const refreshTree = async () => { if (rootFolder?.id) await loadFolder(rootFolder.id); };
  const loadSubFolder = async (folderId) => {
    try {
      const data = await api.folderContents(folderId);
      const replacement = { id:data.folderId, folderName:data.folderName, kind:'folder', loaded:true, folders:(data.folders || []).map(f => ({...f, kind:'folder', loaded:false})), files:data.files || [] };
      const replaceNode = (node) => {
        if (node.id === folderId) return replacement;
        if (!node.folders) return node;
        return {...node, folders:node.folders.map(replaceNode)};
      };
      setTree(current => current ? replaceNode(current) : current);
    } catch (err) { notify(err.message); }
  };
  const saveFile = async () => {
    if (!loggedIn) { setAuthMode('login'); return; }

    const fallback = starterFor(selectedLang);
    const extension = String(activeFile?.fileExtension || fallback.ext).replace(/^\./, '').toLowerCase();
    const fileName = activeFile?.fileName || fallback.name;
    const parent = findFolder(tree, selectedFolderId) || rootFolder;

    try {
      setBusy(true);
      if (activeFile?.id) {
        // Existing workspace file: update the same record, never create a duplicate.
        await api.updateFile(activeFile.id, fileName, code);
        localStorage.removeItem(`oc-draft-${activeFile.id}`);
        setActiveFile(current => ({ ...current, fileContent: code }));
        setSaved(true);
        notify('File saved');
      } else {
        // Scratch file: create it in the workspace only after an explicit Save.
        if (!parent?.id) throw new Error('Workspace folder is not ready. Please refresh and try again.');
        const created = await api.createFile(parent.id, fileName, extension);
        if (!created?.id) throw new Error('File was not created by the server.');
        const savedFile = {
          ...created,
          fileName: created.fileName || fileName,
          fileExtension: created.fileExtension || extension,
          fileContent: code,
        };
        // Store the new ID immediately so a failed content update can be retried
        // against this same file instead of creating a duplicate on the next Save.
        setActiveFile(savedFile);
        setScratchTabs(tabs => tabs.filter(tab => tab.scratchKey !== activeFile?.scratchKey));
        setOpenTabs(tabs => tabs.some(tab => tab.id === savedFile.id) ? tabs : [...tabs, savedFile]);
        await api.updateFile(created.id, savedFile.fileName, code);
        setSaved(true);
        notify('File created and saved to MY-WORKPLACE');
      }
      await refreshTree();
    } catch (err) {
      notify(err.message);
    } finally {
      setBusy(false);
    }
  };

  const findFolder = (node, id) => {
    if (!node || id == null) return null;
    if (node.id === id) return node;
    for (const child of node.folders || []) {
      const found = findFolder(child, id);
      if (found) return found;
    }
    return null;
  };
  const selectedFolder = findFolder(tree, selectedFolderId) || rootFolder;
  const createFolder = (parent = selectedFolder) => setModal({ type:'folder', parent: parent || rootFolder });
  const createFile = (parent = selectedFolder) => setModal({ type:'file', parent: parent || rootFolder });
  const selectFolder = (folder) => setSelectedFolderId(folder.id);
  const submitCreate = async (e) => {
    e.preventDefault(); const fd = new FormData(e.currentTarget); setBusy(true);
    try {
      if (modal.type === 'folder') await api.createFolder(modal.parent.id, fd.get('name'));
      else {
        const raw = fd.get('name').trim(); const ext = fd.get('extension'); const name = raw.replace(/\.[^.]+$/, '');
        const created = await api.createFile(modal.parent.id, name, ext);
        if (created?.id) { setOpenTabs(tabs => tabs.some(tab => tab.id === created.id) ? tabs : [...tabs, created]); await openFile(created); }
      }
      if (modal.parent.id === rootFolder?.id) await refreshTree();
      else await loadSubFolder(modal.parent.id);
      setModal(null); notify(modal.type === 'folder' ? 'Folder created' : 'File created');
    } catch (err) { notify(err.message); } finally { setBusy(false); }
  };
  const renameNode = async (node, type) => {
    const current = type === 'folder' ? node.folderName : node.fileName;
    const next = window.prompt(`Enter new ${type} name`, current);
    if (!next || next.trim() === current) return;
    try {
      if (type === 'folder') await api.updateFolder(node.id, next.trim());
      else { const content = activeFile?.id === node.id ? code : normalizeStoredCode((await api.getFile(node.id)).fileContent || ''); await api.updateFile(node.id, next.trim(), content); }
      await refreshTree(); if (type === 'file' && activeFile?.id === node.id) setActiveFile({...activeFile, fileName:next.trim()});
      notify(`${type === 'folder' ? 'Folder' : 'File'} renamed`);
    } catch (err) { notify(err.message); }
  };
  const deleteNode = async (node, type) => {
    if (!window.confirm(`Delete this ${type}?`)) return;
    try { if (type === 'folder') await api.deleteFolder(node.id); else { await api.deleteFile(node.id); setOpenTabs(tabs => tabs.filter(tab => tab.id !== node.id)); } if (activeFile?.id === node.id) {setActiveFile(null); setCode(STARTERS.java.code);} await refreshTree(); notify(`${type} deleted`); }
    catch (err) { notify(err.message); }
  };

  const runCode = () => {
    const activeExtension = (activeFile?.fileExtension || starterFor(selectedLang).ext).replace(/^\./, '').toLowerCase();
    if (activeExtension === 'html' || languageKey(selectedLang) === 'html') {
      executionRef.current?.close();
      executionRef.current = null;
      setHtmlPreview(true);
      setPreviewExpanded(false);
      setConsoleOpen(false);
      return;
    }
    const selectedMeta = languages.find(item => languageKey(item.name) === languageKey(selectedLang));
    const config = executionLanguage(activeFile?.fileExtension || selectedMeta?.extension || starterFor(selectedLang).ext, selectedLang, languages);
    setConsoleOpen(true); setConsoleTab('Terminal'); setConsoleMaximized(false); setStderr(''); setOutputText(''); setExecutionDetails(''); setInputRequested(false); setStdin(''); outputBufferRef.current = '';
    if (!config) {setConsoleMessage(`Execution is not configured for ${activeFile?.fileExtension || selectedLang}.`); return;}
    if (!code.trim()) {setConsoleMessage('Code editor is empty.'); return;}
    executionRef.current?.close();
    setExecutionStatus('CONNECTING'); setConsoleMessage('Connecting to execution server...');
    const session = createExecution({
      onMessage: msg => {
        if (msg.type === 'OUTPUT') {
          const data = String(msg.data ?? '');
          if (msg.stream === 'stderr') {
            setStderr(prev => prev + data);
          } else {
            setOutputText(prev => prev + data);
            outputBufferRef.current = (outputBufferRef.current + data).slice(-500);
            // Ordinary output does not mean stdin is requested. The input panel
            // is shown only for explicit INPUT_REQUEST / WAITING_FOR_INPUT events.
            setExecutionStatus('RUNNING');
          }
          setConsoleMessage('');
        } else if (msg.type === 'INPUT_REQUEST' || msg.type === 'STDIN_REQUEST' || msg.type === 'WAITING_FOR_INPUT') {
          setInputRequested(true);
          setExecutionStatus('WAITING_FOR_INPUT');
          if (msg.message) setConsoleMessage(msg.message);
        } else if (msg.type === 'STATUS') {
          const nextStatus = String(msg.status || 'RUNNING').toUpperCase();
          setExecutionStatus(nextStatus);
          if (['WAITING_FOR_INPUT','INPUT_REQUIRED','AWAITING_INPUT','STDIN_REQUEST'].includes(nextStatus)) {
            setInputRequested(true);
          }
          if (msg.message) setExecutionDetails(prev => prev + msg.message + '\n');
        } else if (msg.type === 'EXIT') {
          setInputRequested(false);
          setExecutionStatus('EXITED');
          setExecutionDetails(prev => prev + `${msg.stage || 'process'} exited with code ${msg.code}\n`);
        } else if (msg.type === 'ERROR' || msg.type === 'TIMEOUT') {
          setInputRequested(false);
          setStderr(prev => prev + (msg.message || msg.type) + '\n');
          setExecutionStatus(msg.type);
        }
      },
      onError: message => {setStderr(prev => prev + message + '\n'); setExecutionStatus('ERROR');},
      onClose: () => setExecutionStatus(current => ['RUNNING','CONNECTING','COMPILING'].includes(current) ? 'DISCONNECTED' : current),
    });
    executionRef.current = session;
    session.start({language:config.language, version:config.version, filename:activeFile ? currentFileName(activeFile) : config.filename, code});
  };
  const sendStdin = () => {
    if (!stdin.trim()) return;
    try {
      executionRef.current?.input(stdin);
      setOutputText(prev => prev + `\n> ${stdin}\n`);
      setStdin('');
      // Hide after sending; show it again only when the backend requests next input.
      setInputRequested(false);
      setExecutionStatus('RUNNING');
    }
    catch(err) {setStderr(prev => prev + err.message + '\n');}
  };
  const stopExecution = () => {
    executionRef.current?.cancel(); setExecutionStatus('CANCELLED');
  };
  const requestAI = async (message, mode, displayText = message) => {
    if (!loggedIn) { setAuthMode('login'); return; }
    setChat(prev => [...prev, { role:'user', text:displayText }, { role:'assistant', text:'Thinking...', loading:true }]);
    setAiLoading(true);
    try {
      const response = await api.chat(message, mode);
      if (typeof response?.message !== 'string') throw new Error('AI response did not contain a message.');
      setChat(prev => {
        const next = [...prev];
        let index = -1;
        for (let i = next.length - 1; i >= 0; i--) { if (next[i].loading) { index = i; break; } }
        if (index >= 0) next[index] = { role:'assistant', html:response.message };
        return next;
      });
    } catch (err) {
      setChat(prev => {
        const next = [...prev];
        let index = -1;
        for (let i = next.length - 1; i >= 0; i--) { if (next[i].loading) { index = i; break; } }
        if (index >= 0) next[index] = { role:'assistant', text:`Unable to get AI response: ${err.message}` };
        return next;
      });
    } finally { setAiLoading(false); }
  };
  const analyzeCode = () => {
    if (!loggedIn) { setAuthMode('login'); return; }
    setSelectedMode(6);
    setAiOpen(true);
    if (window.matchMedia('(max-width: 760px)').matches) setMobileAI(true);
    // Prefill the Ask Anything composer. The user chooses when to send it.
    setChatInput(`Please analyze this ${selectedLang} code:\n\n${code}`);
  };
  const sendChat = (e) => {
    e.preventDefault();
    const message = chatInput.trim();
    if (!message || aiLoading) return;
    if (!loggedIn) { setAuthMode('login'); return; }
    setChatInput('');
    setModelMenuOpen(false);
    const requestMessage = selectedMode === 6 && !message.includes(code)
      ? `${message}\n\nCurrent ${selectedLang} code:\n\n${code}`
      : message;
    requestAI(requestMessage, selectedMode, message);
  };
  const startAIResize = (event) => {
    if (event.button !== 0) return;
    event.preventDefault();
    resizeDragRef.current = { startX:event.clientX, startWidth:aiWidth };
    const onMove = (moveEvent) => {
      if (!resizeDragRef.current || !workspaceRef.current) return;
      const workspaceWidth = workspaceRef.current.getBoundingClientRect().width;
      const explorerWidth = showExplorer ? (window.innerWidth <= 1050 ? 180 : 205) : 0;
      const maxAllowed = Math.min(workspaceWidth * 0.5, workspaceWidth - explorerWidth - 260 - 30);
      const delta = resizeDragRef.current.startX - moveEvent.clientX;
      setAiWidth(Math.max(245, Math.min(maxAllowed, resizeDragRef.current.startWidth + delta)));
    };
    const onUp = () => {
      resizeDragRef.current = null;
      window.removeEventListener('pointermove', onMove);
      window.removeEventListener('pointerup', onUp);
    };
    window.addEventListener('pointermove', onMove);
    window.addEventListener('pointerup', onUp);
  };
  const logout = async () => {
    try { await api.logout(); } catch (err) { notify(`Logout request failed: ${err.message}`); }
    setLoggedIn(false); setUser(null); setRootFolder(null); setTree(null);
    setActiveFile(null); setOpenTabs([]); setScratchTabs([]); setSelectedFolderId(null);
    setProfileOpen(false); setAiOpen(false); setConsoleMaximized(false);
    setHtmlPreview(false); setPreviewExpanded(false);
    localStorage.removeItem('oc-active-file-id');
    localStorage.removeItem('oc-open-tab-ids');
    notify('Logged out');
  };

  const treeProps = { onOpenFile: openFile, onCreateFolder: createFolder, onCreateFile: createFile, onRename: renameNode, onDelete: deleteNode, onExpandFolder: loadSubFolder, onSelectFolder: selectFolder, selectedFolderId, activeFileId: activeFile?.id };
  const editorTabs = [...scratchTabs, ...openTabs];
  const currentTitle = activeFile ? `${activeFile.fileName}.${(activeFile.fileExtension || '').replace(/^\./,'')}` : `${starterFor(selectedLang).name}.${languages.find(l => languageKey(l.name) === languageKey(selectedLang))?.extension?.replace(/^\./,'') || starterFor(selectedLang).ext}`;
  const showExplorer = loggedIn && explorerOpen;
  const showAI = loggedIn && aiOpen;

  return <div className={`app-shell ${loggedIn ? 'authenticated-mode' : 'guest-mode'} theme-${theme}`}>
    <header className="topbar">
      <div className="brand-area">
        {loggedIn && <button className="hamburger icon-btn" title="Toggle file explorer" onClick={() => {if(window.matchMedia('(max-width: 760px)').matches) setMobileDrawer(v => !v); else setExplorerOpen(v => !v);}}><Menu size={19}/></button>}
        <div className="brand-mark"><Code2 size={22}/></div>
        <div className="brand-copy"><strong>TAVQEN</strong><span>Code. Run. Learn.</span></div>
      </div>
      {/* <div className="workspace-select">{loggedIn ? <><Folder size={14}/> MY-WORKPLACE <ChevronDown size={13}/></> : <span className="guest-label">Online IDE</span>}</div> */}
      <div className="top-actions">
        {loggedIn ? <>
          <button className={`assistant-toggle ${showAI ? 'active' : ''}`} onClick={() => {if(window.matchMedia('(max-width: 760px)').matches) setMobileAI(v => !v); else setAiOpen(v => !v);}}><Sparkles size={14}/> <span>AI Assistant</span></button>
          <button className="icon-btn notification" title="Notifications"><Bell size={18}/></button>
          <div className="profile-wrap"><button className="profile-button" onClick={() => setProfileOpen(v => !v)}><span className="avatar">{(user?.name || 'U').slice(0,1).toUpperCase()}</span><span className="profile-name">{user?.name || 'User'}</span><ChevronDown size={13}/></button>{profileOpen && <div className="profile-menu"><div className="profile-email">{user?.email}</div><button onClick={logout}>Log out</button></div>}</div>
        </> : <><button className="login-btn" onClick={() => {setAuthMode('login');setError('')}}>Login</button><button className="register-btn" onClick={() => {setAuthMode('register');setError('')}}>Register</button></>}
      </div>
    </header>

    <nav className="language-bar" aria-label="Programming languages">
      {languageOptions.map(lang => <button key={lang.id || lang.name} className={`language-pill ${selectedLang.toLowerCase() === lang.name.toLowerCase() ? 'active' : ''}`} onClick={() => selectLanguage(lang)} title={`Open ${lang.name} scratch file`}><span className="lang-icon">{lang.icon ? <img src={lang.icon} alt="" loading="lazy" onError={event => { event.currentTarget.style.display = 'none'; }} /> : <span className="lang-dot">{lang.name.slice(0,1)}</span>}</span>{lang.name}</button>)}
      {loggedIn ? <button className="language-more" title="More languages"><MoreHorizontal size={17}/></button> : null}
    </nav>

    <main ref={workspaceRef} style={showAI ? { '--ai-panel-width': `${aiWidth}px` } : undefined} className={`workspace ${showExplorer ? 'with-explorer' : ''} ${showAI ? 'with-ai' : ''}`}>
      {showExplorer && <aside className="explorer-panel desktop-explorer">
        <div className="panel-heading"><span>EXPLORER</span><div><button title="New folder in selected folder" onClick={() => createFolder(selectedFolder)}><FolderPlus size={15}/></button><button title="New file in selected folder" onClick={() => createFile(selectedFolder)}><FilePlus2 size={15}/></button><button title="Refresh" onClick={refreshTree}><Activity size={14}/></button></div></div>
        <div className="tree-root">{tree ? <TreeNode node={tree} {...treeProps}/> : <div className="tree-empty">Loading workspace...</div>}</div>
        <div className="explorer-footer">
          <button className="settings-launcher" onClick={() => setSettingsOpen(true)}><Settings size={16}/><span>Settings</span></button>
        </div>
      </aside>}

      <section className={`editor-column ${consoleMaximized ? 'console-maximized' : ''} ${htmlPreview ? 'preview-mode' : ''}`}>
        {htmlPreview ? <div className={`html-preview-card ${previewExpanded ? 'preview-expanded' : ''}`}>
          <div className="preview-toolbar">
            <div className="preview-title"><Monitor size={15}/><strong>HTML Preview</strong><span>{currentTitle}</span></div>
            <div className="preview-actions">
              <button onClick={() => setPreviewExpanded(value => !value)} title={previewExpanded ? 'Restore preview' : 'Expand preview'}>{previewExpanded ? <Minimize2 size={15}/> : <Maximize2 size={15}/>}<span>{previewExpanded ? 'Restore' : 'Expand'}</span></button>
              <button onClick={() => {setHtmlPreview(false);setPreviewExpanded(false)}} title="Return to code"><Code2 size={15}/><span>Back to Code</span></button>
            </div>
          </div>
          <iframe className="html-preview-frame" title="HTML output" sandbox="allow-scripts" srcDoc={code}/>
        </div> : <>
        <div className="editor-card">
          <div className="editor-tabbar">
            <button className="new-tab" title="New file in selected folder" onClick={() => loggedIn && tree ? createFile(selectedFolder) : notify('Log in to create workspace files')}><Plus size={17}/></button>
            <div className="editor-tabs-scroll">
              {editorTabs.map(tab => (
                <div key={tab.isScratch ? tab.tabKey : tab.id} className={`file-tab ${(tab.isScratch ? activeFile?.scratchKey === tab.scratchKey : activeFile?.id === tab.id) ? 'active' : ''} ${tab.isScratch ? 'temporary-tab' : ''}`} onClick={() => tab.isScratch ? activateScratchTab(tab) : activateTab(tab)} title={`${currentFileName(tab)}${tab.isScratch ? ' (temporary, not saved)' : ''}`}>
                  <FileCode2 size={13}/><span>{currentFileName(tab)}</span>
                  {tab.isScratch && <span className="temporary-dot" title="Not saved"/>}
                  <button title="Close tab" onClick={(event) => tab.isScratch ? (event.stopPropagation(), setScratchTabs(items => items.filter(item => item.scratchKey !== tab.scratchKey)), activeFile?.scratchKey === tab.scratchKey && (setActiveFile(null), setCode(STARTERS.java.code), setSaved(true))) : closeTab(tab, event)}><X size={12}/></button>
                </div>
              ))}
              {!activeFile && editorTabs.length === 0 && <div className="file-tab active"><FileCode2 size={13}/><span>{currentTitle}</span></div>}
            </div>
          </div>
          <div className="editor-toolbar"><button className="back-control" title="Editor options"><ChevronRight size={15}/></button><div className="save-status">{saved ? <><Check size={13}/> Saved</> : <span className="unsaved-dot">● Unsaved</span>}</div><button className="toolbar-icon" title="Save" onClick={saveFile}><Save size={15}/></button><button className="run-button" onClick={runCode} disabled={busy || ['CONNECTING','COMPILING','RUNNING'].includes(executionStatus)}><Play size={13} fill="currentColor"/> {executionStatus === 'RUNNING' ? 'Running' : 'Run'}</button>{['CONNECTING','COMPILING','RUNNING'].includes(executionStatus) && <button className="stop-button" onClick={stopExecution}>Stop</button>}{loggedIn && <button className="analyze-button" onClick={analyzeCode}><Sparkles size={13}/> Analyze</button>}</div>
          <div className="monaco-wrap"><Editor height="100%" language={extToLanguage[activeFile?.fileExtension?.replace(/^\./,'') || starterFor(selectedLang).ext] || 'java'} theme={theme === 'light' ? 'light' : 'vs-dark'} defaultValue={code} onMount={(editor) => { editorRef.current = editor; editorChangeSubscriptionRef.current?.dispose(); editorChangeSubscriptionRef.current = editor.onDidChangeModelContent(() => { if (!syncingEditorRef.current) { setCode(editor.getValue()); setSaved(false); } }); }} options={{fontFamily:'Consolas, "Courier New", monospace',fontSize:13,lineHeight:21,letterSpacing:0,fontLigatures:false,minimap:{enabled:false},padding:{top:10,bottom:10},scrollBeyondLastLine:false,wordWrap:'off',automaticLayout:true,renderLineHighlight:'all',overviewRulerBorder:false,hideCursorInOverviewRuler:true,cursorStyle:'line',cursorBlinking:'smooth',scrollbar:{verticalScrollbarSize:8,horizontalScrollbarSize:8}}}/></div>
        </div>
        <section className={`console-card ${consoleOpen ? 'expanded' : 'collapsed'} ${consoleMaximized ? 'maximized' : ''}`}>
          <div className="console-tabs">
            <button className={consoleTab === 'Terminal' ? 'active' : ''} onClick={() => {setConsoleTab('Terminal');setConsoleOpen(true)}}>Terminal</button>
            <button className={consoleTab === 'Details' ? 'active' : ''} onClick={() => {setConsoleTab('Details');setConsoleOpen(true)}}>Details</button>
            <button className="console-expand" title={consoleMaximized ? 'Restore editor size' : 'Expand output panel'} onClick={() => {setConsoleOpen(true);setConsoleMaximized(v => !v)}}>{consoleMaximized ? <Minimize2 size={14}/> : <Maximize2 size={14}/>}</button>
            <button className="console-collapse" title={consoleOpen ? 'Collapse console' : 'Expand console'} onClick={() => setConsoleOpen(v => !v)}>{consoleOpen ? '⌄' : '⌃'}</button>
          </div>
          {consoleOpen && <div className="console-content">
            {consoleTab === 'Details' ? <pre className="terminal-text">Status: {executionStatus || 'IDLE'}\n{executionDetails || 'Execution details will appear here.'}</pre> : <>
              <div className="terminal-stream">
                <div className="terminal-section-label">OUTPUT</div>
                <pre className="terminal-text">{outputText || consoleMessage}</pre>
                {stderr.trim() && <><div className="terminal-section-label error-label">ERRORS</div><pre className="terminal-text error-text">{stderr}</pre></>}
              </div>
              {inputRequested && <div className="stdin-panel">
                <div className="stdin-hint"><span className="stdin-live-dot"/> Program input <span>Send input while your program is running</span></div>
                <div className="stdin-controls">
                  <span className="stdin-prefix">stdin &gt;</span>
                  <input value={stdin} onChange={e => setStdin(e.target.value)} onKeyDown={e => {if(e.key === 'Enter'){e.preventDefault();sendStdin();}}} placeholder="Type input and press Enter..." aria-label="Program standard input" />
                  <button onClick={sendStdin} disabled={!stdin.trim()}>Send</button>
                </div>
              </div>}
            </>}
          </div>}
          {!consoleOpen && <button className="console-empty" onClick={() => setConsoleOpen(true)}><Play size={15}/> Run your code to see the output here...</button>}
        </section>
        </>}
      </section>

      {showAI && <>
        <div className="ai-resize-handle" role="separator" aria-orientation="vertical" aria-label="Resize AI assistant" title="Drag to resize AI assistant" onPointerDown={startAIResize}><span/></div>
        <aside className="ai-panel desktop-ai">
          <div className="ai-header"><div className="ai-title-icon"><Bot size={16}/></div><strong>AI Coding Assistant <Sparkles size={12}/></strong><div className="ai-head-actions"><button title="Minimize" onClick={() => setAiOpen(false)}>−</button><button title="Close" onClick={() => setAiOpen(false)}><X size={15}/></button></div></div>
          <div className="chat-messages" ref={desktopChatRef}>{chat.map((m,i) => <div key={i} className={`chat-message ${m.role}`}><div className="message-avatar">{m.role === 'assistant' ? <Bot size={13}/> : (user?.name || 'U').slice(0,1).toUpperCase()}</div><div className="message-body">{m.role === 'assistant' && <strong>AI Assistant</strong>}{m.html !== undefined ? <div className="ai-html-response" dangerouslySetInnerHTML={{ __html:m.html }}/> : <p>{m.text}</p>}{m.role === 'assistant' && i === 0 && <div className="suggestions"><button onClick={() => setChatInput('Explain this code and its time complexity.')}>Explain this code</button><button onClick={() => setChatInput('Find bugs in my code.')}>Find bugs</button><button onClick={() => setChatInput('Optimize this code.')}>Optimize code</button></div>}</div></div>)}</div>
          <form className="chat-composer" onSubmit={sendChat}>
            <div className="model-picker-wrap">
              <button type="button" className={`model-picker-trigger ${modelMenuOpen ? 'active' : ''}`} onClick={() => setModelMenuOpen(v => !v)} aria-expanded={modelMenuOpen}><Sparkles size={12}/><span>{AI_MODES.find(mode => mode.value === selectedMode)?.label || 'Ask Anything'}</span><ChevronDown size={12}/></button>
              {modelMenuOpen && <div className="model-picker-menu">{AI_MODES.map(mode => <button type="button" key={mode.value} className={selectedMode === mode.value ? 'selected' : ''} onClick={() => {setSelectedMode(mode.value);setModelMenuOpen(false)}}><span className="model-option-label">{mode.label}</span><span className="model-option-description">{mode.description}</span></button>)}</div>}
            </div>
            <textarea className="chat-prompt-input" rows={3} value={chatInput} onChange={e => setChatInput(e.target.value)} placeholder="Ask anything about your code..." disabled={aiLoading}/><button type="submit" disabled={aiLoading || !chatInput.trim()}><Send size={15}/></button>
          </form>
        </aside>
      </>}
    </main>

    <div className="mobile-bottom-actions">{loggedIn && <><button onClick={() => setMobileDrawer(true)}><Menu size={17}/> Explorer</button><button onClick={() => setMobileAI(true)}><Sparkles size={16}/> AI Assistant</button><button onClick={() => setSettingsOpen(true)}><Settings size={16}/> Settings</button></>}</div>
    {mobileDrawer && loggedIn && <div className="mobile-overlay" onMouseDown={() => setMobileDrawer(false)}><aside className="mobile-drawer" onMouseDown={e => e.stopPropagation()}><div className="panel-heading"><span>EXPLORER</span><button onClick={() => setMobileDrawer(false)}><X size={17}/></button></div>{tree && <TreeNode node={tree} {...treeProps}/>}<div className="explorer-footer"><button className="settings-launcher" onClick={() => {setMobileDrawer(false);setSettingsOpen(true)}}><Settings size={16}/><span>Settings</span></button></div></aside></div>}
    {mobileAI && loggedIn && <div className="mobile-ai-screen"><div className="ai-header"><div className="ai-title-icon"><Bot size={16}/></div><strong>AI Coding Assistant</strong><button onClick={() => setMobileAI(false)}><X size={17}/></button></div><div className="chat-messages" ref={mobileChatRef}>{chat.map((m,i)=><div key={i} className={`chat-message ${m.role}`}><div className="message-avatar">{m.role==='assistant'?<Bot size={13}/> : 'U'}</div><div className="message-body">{m.role==='assistant' && <strong>AI Assistant</strong>}{m.html !== undefined ? <div className="ai-html-response" dangerouslySetInnerHTML={{__html:m.html}}/> : <p>{m.text}</p>}</div></div>)}</div><form className="chat-composer" onSubmit={sendChat}><div className="model-picker-wrap"><button type="button" className={`model-picker-trigger ${modelMenuOpen ? 'active' : ''}`} onClick={() => setModelMenuOpen(v=>!v)}><Sparkles size={12}/><span>{AI_MODES.find(mode=>mode.value===selectedMode)?.label || 'Ask Anything'}</span><ChevronDown size={12}/></button>{modelMenuOpen && <div className="model-picker-menu">{AI_MODES.map(mode=><button type="button" key={mode.value} className={selectedMode===mode.value?'selected':''} onClick={()=>{setSelectedMode(mode.value);setModelMenuOpen(false)}}><span className="model-option-label">{mode.label}</span><span className="model-option-description">{mode.description}</span></button>)}</div>}</div><textarea className="chat-prompt-input" rows={3} value={chatInput} onChange={e=>setChatInput(e.target.value)} placeholder="Ask anything about your code..." disabled={aiLoading}/><button type="submit" disabled={aiLoading || !chatInput.trim()}><Send size={15}/></button></form></div>}

    {settingsOpen && <Modal title="Settings" onClose={() => setSettingsOpen(false)}>
      <div className="settings-layout">
        <div className="settings-sidebar"><div className="settings-sidebar-item active"><Palette size={15}/> Theme</div></div>
        <div className="settings-content">
          <strong>Choose your theme</strong>
          <p>Personalize the editor appearance. Your choice is saved on this device.</p>
          <div className="theme-options">
            {[
              {id:'midnight',name:'Midnight',hint:'Classic dark blue',icon:<Moon size={16}/>},
              {id:'light',name:'Daylight',hint:'Clean light theme',icon:<Sun size={16}/>},
              {id:'ocean',name:'Ocean',hint:'Deep teal and blue',icon:<Palette size={16}/>},
              {id:'purple',name:'Amethyst',hint:'Dark violet theme',icon:<Sparkles size={16}/>}
            ].map(option => <button key={option.id} className={`theme-option ${theme === option.id ? 'selected' : ''}`} onClick={() => setTheme(option.id)}>
              <span className={`theme-swatch ${option.id}`}>{option.icon}</span>
              <span><strong>{option.name}</strong><small>{option.hint}</small></span>
              {theme === option.id && <Check size={16}/>}
            </button>)}
          </div>
        </div>
      </div>
    </Modal>}
    {authMode && <Modal title={authMode === 'login' ? 'Welcome back' : 'Create your account'} onClose={() => setAuthMode('')}><form className="auth-form" onSubmit={submitAuth}>{authMode === 'register' && <label>Full name<input name="name" placeholder="Enter your name" required/></label>}<label>Email address<input name="email" type="email" placeholder="you@example.com" required/></label><label>Password<input name="password" type="password" placeholder="Enter password" required minLength={6}/></label>{authMode === 'register' && <label>Gender<select name="gender" defaultValue="MALE"><option value="MALE">Male</option><option value="FEMALE">Female</option><option value="OTHER">Other</option></select></label>}{error && <div className="form-error">{error}</div>}<button className="primary-submit" disabled={busy}>{busy ? 'Please wait...' : authMode === 'login' ? 'Login' : 'Create account'}</button><p className="switch-auth">{authMode === 'login' ? 'New here?' : 'Already have an account?'} <button type="button" onClick={() => {setError('');setAuthMode(authMode === 'login' ? 'register' : 'login')}}>{authMode === 'login' ? 'Create account' : 'Login'}</button></p></form></Modal>}
    {modal && <Modal title={modal.type === 'folder' ? 'Create new folder' : 'Create new file'} onClose={() => setModal(null)}><form className="auth-form" onSubmit={submitCreate}><label>{modal.type === 'folder' ? 'Folder name' : 'File name'}<input name="name" placeholder={modal.type === 'folder' ? 'e.g. Java Projects' : 'e.g. Main'} required autoFocus/></label>{modal.type === 'file' && <label>Extension<select name="extension" defaultValue="java">{['java','py','c','cpp','js','ts','html','css','txt','sql','go','rs','php','rb','kt','swift','cs'].map(ext=><option key={ext} value={ext}>.{ext}</option>)}</select></label>}<button className="primary-submit" disabled={busy}>{busy ? 'Creating...' : 'Create'}</button></form></Modal>}
    {toast && <div className="toast"><Check size={15}/>{toast}</div>}
  </div>;
}
