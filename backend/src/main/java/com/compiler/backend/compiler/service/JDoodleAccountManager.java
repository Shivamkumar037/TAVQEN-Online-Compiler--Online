package com.compiler.backend.compiler.service;
import com.compiler.backend.compiler.config.CompilerProperties;
import org.springframework.stereotype.Component;
import java.time.LocalDate; import java.util.*; import java.util.concurrent.atomic.AtomicInteger;
@Component
public class JDoodleAccountManager {
 private final CompilerProperties props; private final Map<String,Usage> usage=new HashMap<>(); private final AtomicInteger cursor=new AtomicInteger();
 public JDoodleAccountManager(CompilerProperties p){props=p;}
 public synchronized List<CompilerProperties.Account> eligibleAccounts(){
  List<CompilerProperties.Account> all=props.getJdoodle().getAccounts().stream().filter(CompilerProperties.Account::isEnabled)
   .filter(a->has(a.getId())&&has(a.getClientId())&&has(a.getClientSecret())).toList();
  if(all.isEmpty()) return List.of(); int start=Math.floorMod(cursor.getAndIncrement(),all.size()); List<CompilerProperties.Account> ordered=new ArrayList<>();
  for(int i=0;i<all.size();i++){var a=all.get((start+i)%all.size()); Usage u=current(a); if(!u.exhausted&&u.credits + 2 <= props.getJdoodle().getLocalDailyCreditLimit()) ordered.add(a);}
  return ordered;
 }
 public synchronized void recordExecutionReservation(CompilerProperties.Account a){current(a).credits += 2;}
 public synchronized void markExhausted(CompilerProperties.Account a){current(a).exhausted=true;}
 private Usage current(CompilerProperties.Account a){Usage u=usage.get(a.getId()); if(u==null||!u.day.equals(LocalDate.now())){u=new Usage(LocalDate.now());usage.put(a.getId(),u);}return u;}
 private boolean has(String s){return s!=null&&!s.isBlank();}
 private static class Usage{LocalDate day;int credits;boolean exhausted;Usage(LocalDate d){day=d;}}
}
