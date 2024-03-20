class GMPChart{gmp_container;container_height;container_width;container_padding_top=50;container_padding_right=50;container_padding_bottom=80;container_padding_left=50;x_ratio;y_ratio;classes;self;chart_data={base:[1,1.4,1.8,2.2,2.5,2.8,3,3.2,3.5,3.7,3.9,4,4.2,4.4,4.5,4.6,4.8,4.9,5,5.1,5.2,5.3,5.4,5.4,5.5],median:[3.2,4.2,5.1,5.8,6.4,6.9,7.3,7.6,7.9,8.2,8.5,8.7,8.9,9.2,9.4,9.6,9.8,10,10.2,10.4,10.6,10.9,11.1,11.3,11.5],z2p:[4.2,5.5,6.5,7.5,8.1,8.7,9.3,9.8,10.2,10.5,10.9,11.2,11.5,11.8,12,12.4,12.6,12.9,13.2,13.5,13.7,14,14.3,14.6,14.8],z1p:[3.7,4.7,5.8,6.5,7.3,7.8,8.2,8.6,9,9.3,9.6,9.9,10.1,10.4,10.6,10.9,11.1,11.4,11.6,11.8,12.1,12.3,12.5,12.8,13],z1m:[2.8,3.6,4.5,5.2,5.7,6.1,6.5,6.8,7,7.3,7.5,7.7,7.9,8.1,8.3,8.5,8.7,8.9,9.1,9.2,9.4,9.6,9.8,10,10.2],z2m:[2.4,3.2,3.9,4.5,5,5.4,5.7,6,6.3,6.5,6.7,6.9,7,7.2,7.4,7.6,7.7,7.9,8.1,8.2,8.4,8.6,8.7,8.9,9],z3m:[2,2.7,3.4,4,4.4,4.8,5.1,5.3,5.6,5.8,5.9,6.1,6.3,6.4,6.6,6.7,6.7,7,7.2,7.3,7.5,7.6,7.8,7.9,8.1],z3p:[5,6.3,7.5,8.5,9.3,10,10.6,11.1,11.5,12,12.4,12.8,13.1,13.5,13.8,14.1,14.5,14.8,15.1,15.4,15.7,16,16.4,16.7,17],age:[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24]};girl_24_60_chart_data={age:[24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60],base:[6,6.1,6.2,6.3,6.4,6.5,6.5,6.6,6.7,6.8,6.9,7,7,7.1,7.2,7.3,7.4,7.4,7.5,7.6,7.7,7.8,7.9,7.9,8,8,8,8.2,8.3,8.4,8.5,8.6,8.7,8.8,8.9,8.9,9],z3m:[8.1,8.2,8.4,8.5,8.6,8.8,8.9,9,9.1,9.3,9.4,9.5,9.6,9.7,9.8,9.9,10.1,10.2,10.3,10.4,10.5,10.6,10.7,10.8,10.9,11,11.1,11.2,11.3,11.4,11.5,11.6,11.7,11.8,11.9,12,12.1],z2m:[9,9.2,9.4,9.5,9.7,9.8,10,10.1,10.3,10.4,10.5,10.7,10.8,10.9,11.12,11.2,11.3,11.5,11.6,11.7,11.8,12,12.1,12.2,12.3,12.4,12.5,12.7,12.8,12.9,13,13.2,13.3,13.4,13.5,13.6,13.7],z1m:[10.2,10.3,10.5,10.7,10.9,11.1,11.2,11.4,11.6,11.7,11.9,12,12.2,12.3,12.5,12.7,12.8,13,13.1,13.2,13.4,13.6,13.7,13.9,14,14.2,14.3,14.5,14.6,14.8,14.9,15.1,15.2,15.3,15.5,15.6,15.8],median:[11.5,11.7,11.9,12.1,12.3,12.5,12.7,12.9,13.1,13.3,13.5,13.7,13.9,14,14.2,14.4,14.6,14.8,15,15.2,15.3,15.5,15.7,15.9,16.1,16.3,16.4,16.6,16.8,17,17.2,17.3,17.5,17.8,17.9,18,18.2],z1p:[13,13.3,13.5,13.7,14,14.2,14.4,14.7,14.8,15.1,15.4,15.6,15.8,16,16.3,16.5,16.7,16.8,17.2,17.4,17.6,17.9,18,18.1,18.3,18.5,19,19.2,19.4,19.8,19.9,20.1,20.3,20.6,20.8,21,21.2],z2p:[14.8,15.1,15.4,15.7,16,16.2,16.5,16.9,17.1,17.3,17.6,17.9,18.1,18.4,18.7,19,19.2,19.5,19.8,20.1,20.4,20.7,20.9,21.2,21.5,21.8,22.1,22.4,22.6,22.9,23.2,23.5,23.8,24.1,24.4,24.6,24.9],z3p:[17,17.3,17.7,18,18.3,18.7,19,19.3,19.7,20,20.3,20.6,20.9,21.3,21.6,22,22.3,22.7,23,23.4,23.7,24.1,24.5,24.8,25.1,25.5,25.9,26.3,26.6,27,27.4,27.7,28.1,28.5,28.8,29.2,29.5]};girl_height_0_24_chart_data={age:[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24],base:[40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40],z3m:[44,47,51,53.5,55.5,57.5,58.5,60,61.5,62.5,64,65.1,66,67.5,68.5,69.5,70.5,71,72,72.5,73.5,74.5,75.5,76.5,77],z2m:[45.5,49.5,53,55.5,57.5,59.5,61.5,62.5,64,65.5,66.5,67.5,68.5,70,71,72,73,74,75.5,76.5,77.5,78.5,79.5,80,80],z1m:[47,51.5,55,57.5,59.5,61.5,63.5,65,66.5,68,69,70.5,71.5,72.5,73.5,74.5,75.1,76.1,77.1,79.5,80.5,81.5,82.5,83.5,83.5],median:[49.5,53.5,57,59.5,62,64,65.5,67,68.5,70.5,71.5,72.5,74,75.5,76.5,77.5,78.5,79.5,80.5,81.5,82.5,83,83.5,84.5,85.5,86.5],z1p:[51,55.5,59.5,62,64.5,66,68,69.5,71,72.5,73.5,75.5,76.5,77.5,79,80.5,81.5,83,83.5,84.5,86.5,87.5,88.5,88.5,89],z2p:[53,56.5,61,64,66.5,67.5,70.5,71.5,73.5,75,76.5,77.5,79,80.5,81.5,82,84.5,85.5,86.5,87.5,89.5,90.5,92,92.5,93.5],z3p:[55,59.5,63,66.5,68,70.5,72,74,75.5,77.5,78,80,81,83,84,85,87,88,89,90.5,91.5,92.5,93,95,96.5]};girl_height_24_60_chart_data={age:[24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60],base:[60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60,60],z3m:[76,77,78,78.5,79,79.5,80.5,81,81.5,82,82.5,83.5,83.5,84.5,84.5,86,86,86.5,86.5,87,87,88,89,89.5,90,90.5,91,91.5,92,92.5,93,93,93.5,93.5,94.5,95,95.5],z2m:[79,80,81,81.5,82.5,82.5,83.5,84.5,84.5,85.5,86.5,86.5,87.5,88,89,90,90,90.5,91,91.5,92,92.5,93,94,94.5,94.5,95,95,96,96,97,97,98.5,98.5,99,100,100],z1m:[83,84,84.5,84.5,86,87,87.5,87.5,89,89.5,89.5,91,91.5,92,93,93.5,94,94.5,95,96,97,97,97.5,97.5,98.5,99,99.5,100,100.5,101,102,103,103,104,104,104.5,104.5],median:[86,87,88,88.5,89,90,90.5,90.5,92,93.5,93.5,94,95,96,97,97.5,98.5,99,99.5,100,100.5,101,101.5,102,102,103,104,104.5,105,105.5,106,107,107,108,108,109,109.5],z1p:[89,90,91,92,93,93.5,94,95,96,96.5,97,98,99.5,100,101,102,102.5,103,103,105,106,106.5,107,107,108,108.5,109,109.5,110,111,111.5,112.5,113,113.5,114,114.5,114.5],z2p:[92.5,93.5,94.5,95,96,97,97.5,98,99,100.5,101.5,102,102.5,103,104,105,105.5,106,107,108,109,109.9,110,111,111.5,112,113,114,115,115.5,116,117,118,119,119,119,119.5],z3p:[95,96,97,98,99,100,101,102,103,103,104,105,106,107,108,108,109,110,111,112,112,113,114,114,115,116,117,117,118,119,119,120,121,121,122,123,123.5]};generate_points_of_axis_json(t,a){var i=[],e=0;return t.forEach(function(t){i.push({x:t,y:a[e++]})}),i}constructor(t){this.self=this,this.gmp_container=document.getElementById(t),this.container_height=this.gmp_container.clientHeight,this.container_width=this.gmp_container.clientWidth,this.x_ratio=(this.gmp_container.clientWidth-this.container_padding_right-this.container_padding_left)/24,this.y_ratio=(this.gmp_container.clientHeight-this.container_padding_top-this.container_padding_bottom)/100}get_x(t){return this.container_padding_left+t*this.x_ratio}get_y(t){return this.container_height-this.container_padding_bottom-this.y_ratio*t}draw_grid(t,a,i,e,_,s=0,n=0){for(let h=t;h<=i;h++)this.draw_poly_line([{x:h,y:a},{x:h,y:e}],.3,_),this.draw_text(this.get_x(h)-5,this.get_y(a)+20,this.bangla_num_converter(s++),"gray","bangla-text","rotate(-90 "+(this.get_x(h)+5)+","+(this.get_y(a)+20)+")");var r=0;for(let $=a;$<=e;$++)0==r||5==r?(this.draw_poly_line([{x:t,y:$},{x:i+.2,y:$}],.7,_),this.draw_text(this.get_x(i)+10,this.get_y($)+5,this.bangla_num_converter(n++),"gray","bangla-text"),r=1):(this.draw_poly_line([{x:t,y:$},{x:i,y:$}],.3,_),n++,r++)}fill_poly(t,a,i,e,_,s,n=1){var h=t.generate_points_of_axis_json(a,i);t.generate_points_of_axis_json(a,e).reverse().forEach(function(t){h.push(t)}),this.draw_poly_line(h,1,_,s,n)}draw_poly_line(t,a=.5,i="black",e="none",_="1"){let s=this,n="";t.forEach(function(t){n+=s.get_x(t.x).toString()+","+s.get_y(t.y).toString()+" "});var h=document.createElementNS("http://www.w3.org/2000/svg","polyline");h.setAttribute("points",n),h.style.stroke=i,h.style.strokeWidth=a,h.style.fill=e,s.gmp_container.appendChild(h)}draw_text(t,a,i,e="black",_="",s=""){var n=document.createElementNS("http://www.w3.org/2000/svg","text");n.setAttributeNS(null,"x",t),n.setAttributeNS(null,"y",a),n.setAttribute("class",_),n.setAttribute("transform",s),n.innerHTML=i,n.style.fill=e,this.gmp_container.appendChild(n)}draw_line(t,a=.5,i="black",e="none"){let _=this,s="";t.forEach(function(t){s+=_.get_x(t.x).toString()+","+_.get_y(t.y).toString()+" "});var n=document.createElementNS("http://www.w3.org/2000/svg","polyline");n.setAttribute("points",s),n.style.stroke=i,n.style.strokeWidth=a,n.style.fill=e,_.gmp_container.appendChild(n)}draw_circle(t,a,i,e,_,s,n=""){let h=document.createElementNS("http://www.w3.org/2000/svg","circle");h.setAttribute("cx",t),h.setAttribute("cy",a),h.setAttribute("r",i),h.setAttribute("class",n),h.style.stroke=_,h.style.strokeWidth=e,h.style.fill=s,this.gmp_container.appendChild(h)}draw_rect(t,a,i,e,_="black",s=1,n=""){var h=document.createElementNS("http://www.w3.org/2000/svg","rect");h.setAttribute("x",t),h.setAttribute("y",a),h.setAttribute("height",e),h.setAttribute("width",i),h.setAttribute("fill",n),h.setAttribute("stroke",_),this.gmp_container.appendChild(h)}bangla_num_converter(t){let a=["০","১","২","৩","৪","৫","৬","৭","৮","৯"],i=Math.abs(t).toString(),e=i.split("").map(t=>"."===t?".":Number(t)),_="";return e.forEach(function(t){"."!=t?_+=a[t]:_+=t}),_}draw_chart(t){}draw_chart_gmp_base_girls_24_60(){this.draw_gmp_base_girls_24_60()}base_pont_adjust(t,a=0,i=0){var e=[];return t.forEach(function(t){e.push({x:t.x-a,y:t.y-i})}),e}point_adjust(t,a){var i=[];return t.forEach(function(t){i.push(t-a)}),i}legend(){this.draw_rect(100,this.container_height-23,15,15,"rgb(0,0,0)",1,"rgb(247, 168, 96)"),this.draw_text(120,this.container_height-10,"মারাত্মক খর্ব","","legend-text"),this.draw_rect(210,this.container_height-23,15,15,"rgb(0,0,0)",1,"#d5bc3a"),this.draw_text(230,this.container_height-10,"মাঝারি খর্ব","","legend-text"),this.draw_rect(310,this.container_height-23,15,15,"rgb(0,0,0)",1,"#e4de69"),this.draw_text(330,this.container_height-10,"স্বল্প খর্ব","","legend-text"),this.draw_rect(400,this.container_height-23,15,15,"rgb(0,0,0)",1,"#9fd193"),this.draw_text(420,this.container_height-10,"স্বাভাবিক","","legend-text"),this.draw_rect(500,this.container_height-23,15,15,"rgb(0,0,0)",1,"#FFF"),this.draw_text(520,this.container_height-10,"বেশি লম্বা","","legend-text")}legend_w(){this.draw_rect(100,this.container_height-23,15,15,"rgb(0,0,0)",1,"rgb(247, 168, 96)"),this.draw_text(120,this.container_height-10,"মারাত্মক অপুষ্টি","","legend-text"),this.draw_rect(210,this.container_height-23,15,15,"rgb(0,0,0)",1,"#d5bc3a"),this.draw_text(230,this.container_height-10,"মাঝারি অপুষ্টি","","legend-text"),this.draw_rect(310,this.container_height-23,15,15,"rgb(0,0,0)",1,"#e4de69"),this.draw_text(330,this.container_height-10,"স্বল্প অপুষ্টি","","legend-text"),this.draw_rect(400,this.container_height-23,15,15,"rgb(0,0,0)",1,"#9fd193"),this.draw_text(420,this.container_height-10,"স্বাভাবিক","","legend-text"),this.draw_rect(500,this.container_height-23,15,15,"rgb(0,0,0)",1,"#FFF"),this.draw_text(520,this.container_height-10,"বেশি ওজন","","legend-text")}chart_title(t="",a=10,i=10){this.draw_text(a,i,t,"","title-text")}legend_text(t="",a=10,i=10,e=0){this.draw_text(a,i,t,"","chart-text","rotate("+e+"  "+a+","+i+")")}girl_weight_gain_chart_0_24(t=""){let a=this;this.x_ratio=(this.gmp_container.clientWidth-this.container_padding_right-this.container_padding_left)/24,this.y_ratio=(this.gmp_container.clientHeight-this.container_padding_top-this.container_padding_bottom)/17,this.fill_poly(this,this.chart_data.age,this.chart_data.base,this.chart_data.z3m,"none","#F4A65F",1),this.fill_poly(this,this.chart_data.age,this.chart_data.z2m,this.chart_data.z3m,"none","#F0CF23",1),this.fill_poly(this,this.chart_data.age,this.chart_data.z2m,this.chart_data.z1m,"none","#F4ED64",1),this.fill_poly(this,this.chart_data.age,this.chart_data.z1m,this.chart_data.z2p,"none","#9FD193",1),this.draw_grid(0,0,24,17,"#858484");var i=this.generate_points_of_axis_json(this.chart_data.age,this.chart_data.base);this.draw_poly_line(i,1,"black"),i=this.generate_points_of_axis_json(this.chart_data.age,this.chart_data.z3m),this.draw_poly_line(i,1,"black"),i=this.generate_points_of_axis_json(this.chart_data.age,this.chart_data.median),this.draw_poly_line(i,1,"black"),i=this.generate_points_of_axis_json(this.chart_data.age,this.chart_data.z2p),this.draw_poly_line(i,1,"black"),i=this.generate_points_of_axis_json(this.chart_data.age,this.chart_data.z1p),this.draw_poly_line(i,1,"black"),i=this.generate_points_of_axis_json(this.chart_data.age,this.chart_data.z1m),this.draw_poly_line(i,1,"black"),i=this.generate_points_of_axis_json(this.chart_data.age,this.chart_data.z3p),this.draw_poly_line(i,1,"black"),i=this.generate_points_of_axis_json(this.chart_data.age,this.chart_data.z2m),this.draw_poly_line(i,1,"black"),this.chart_data.age.forEach(function(t){a.draw_circle(a.get_x(t),a.get_y(a.chart_data.base[t]),2,1,"black","","base-point"),a.draw_circle(a.get_x(t),a.get_y(a.chart_data.median[t]),2,1,"black","","base-point"),a.draw_circle(a.get_x(t),a.get_y(a.chart_data.z1m[t]),2,1,"black","","base-point"),a.draw_circle(a.get_x(t),a.get_y(a.chart_data.z1p[t]),2,1,"black","","base-point"),a.draw_circle(a.get_x(t),a.get_y(a.chart_data.z2m[t]),2,1,"black","","base-point"),a.draw_circle(a.get_x(t),a.get_y(a.chart_data.z2p[t]),2,1,"black","","base-point"),a.draw_circle(a.get_x(t),a.get_y(a.chart_data.z3m[t]),2,1,"black","","base-point"),a.draw_circle(a.get_x(t),a.get_y(a.chart_data.z3p[t]),2,1,"black","","base-point")}),this.draw_poly_line(t,2,"red","none"),this.legend_w(),this.chart_title("মেয়ে শিশুর ওজন বৃদ্ধির চার্ট (০ - ২৪ মাস)",180,30),this.legend_text("বয়স (মাস)",340,470),this.legend_text("ওজন বৃদ্ধি (কেজি)",30,260,-90)}girl_weight_gain_chart_24_60(t=""){let a=this;this.x_ratio=(this.gmp_container.clientWidth-this.container_padding_right-this.container_padding_left)/36,this.y_ratio=(this.gmp_container.clientHeight-this.container_padding_top-this.container_padding_bottom)/23,this.fill_poly(this,this.point_adjust(this.girl_24_60_chart_data.age,24),this.point_adjust(this.girl_24_60_chart_data.base,6),this.point_adjust(this.girl_24_60_chart_data.z3m,6),"none","#F4A65F",1),this.fill_poly(this,this.point_adjust(this.girl_24_60_chart_data.age,24),this.point_adjust(this.girl_24_60_chart_data.z2m,6),this.point_adjust(this.girl_24_60_chart_data.z3m,6),"none","#F0CF23",1),this.fill_poly(this,this.point_adjust(this.girl_24_60_chart_data.age,24),this.point_adjust(this.girl_24_60_chart_data.z2m,6),this.point_adjust(this.girl_24_60_chart_data.z1m,6),"none","#F4ED64",1),this.fill_poly(this,this.point_adjust(this.girl_24_60_chart_data.age,24),this.point_adjust(this.girl_24_60_chart_data.z1m,6),this.point_adjust(this.girl_24_60_chart_data.z2p,6),"none","#9FD193",1),this.draw_grid(0,0,36,30,"#858484",24,6);var i=this.generate_points_of_axis_json(this.girl_24_60_chart_data.age,this.girl_24_60_chart_data.base),e=this.base_pont_adjust(i,24,6);this.draw_poly_line(e,1,"black"),i=this.generate_points_of_axis_json(this.girl_24_60_chart_data.age,this.girl_24_60_chart_data.z3m);var e=this.base_pont_adjust(i,24,6);this.draw_poly_line(e,1,"black"),i=this.generate_points_of_axis_json(this.girl_24_60_chart_data.age,this.girl_24_60_chart_data.median);var e=this.base_pont_adjust(i,24,6);this.draw_poly_line(e,1,"black"),i=this.generate_points_of_axis_json(this.girl_24_60_chart_data.age,this.girl_24_60_chart_data.z2p);var e=this.base_pont_adjust(i,24,6);this.draw_poly_line(e,1,"black"),i=this.generate_points_of_axis_json(this.girl_24_60_chart_data.age,this.girl_24_60_chart_data.z1p);var e=this.base_pont_adjust(i,24,6);this.draw_poly_line(e,1,"black"),i=this.generate_points_of_axis_json(this.girl_24_60_chart_data.age,this.girl_24_60_chart_data.z1m);var e=this.base_pont_adjust(i,24,6);this.draw_poly_line(e,1,"black"),i=this.generate_points_of_axis_json(this.girl_24_60_chart_data.age,this.girl_24_60_chart_data.z3p);var e=this.base_pont_adjust(i,24,6);this.draw_poly_line(e,1,"black"),i=this.generate_points_of_axis_json(this.girl_24_60_chart_data.age,this.girl_24_60_chart_data.z2m);var e=this.base_pont_adjust(i,24,6);this.draw_poly_line(e,1,"black");var _=0;this.girl_24_60_chart_data.age.forEach(function(t){console.log(t),a.draw_circle(a.get_x(t-24),a.get_y(a.girl_24_60_chart_data.base[_]-6),2,1,"black","","base-point"),a.draw_circle(a.get_x(t-24),a.get_y(a.girl_24_60_chart_data.median[_]-6),2,1,"black","","base-point"),a.draw_circle(a.get_x(t-24),a.get_y(a.girl_24_60_chart_data.z1m[_]-6),2,1,"black","","base-point"),a.draw_circle(a.get_x(t-24),a.get_y(a.girl_24_60_chart_data.z1p[_]-6),2,1,"black","","base-point"),a.draw_circle(a.get_x(t-24),a.get_y(a.girl_24_60_chart_data.z2m[_]-6),2,1,"black","","base-point"),a.draw_circle(a.get_x(t-24),a.get_y(a.girl_24_60_chart_data.z2p[_]-6),2,1,"black","","base-point"),a.draw_circle(a.get_x(t-24),a.get_y(a.girl_24_60_chart_data.z3m[_]-6),2,1,"black","","base-point"),a.draw_circle(a.get_x(t-24),a.get_y(a.girl_24_60_chart_data.z3p[_]-6),2,1,"black","","base-point"),_++}),this.draw_poly_line(this.base_pont_adjust(t,24,6),2,"red","none"),this.legend_w(),this.chart_title("মেয়ে শিশুর ওজন বৃদ্ধির চার্ট (২৪ - ৬০ মাস)",180,30),this.legend_text("বয়স (মাস)",340,470),this.legend_text("ওজন বৃদ্ধি (কেজি)",30,260,-90)}girl_height_gain_chart_0_24(t=""){let a=this;var i=this.girl_height_0_24_chart_data,e=40;this.x_ratio=(this.gmp_container.clientWidth-this.container_padding_right-this.container_padding_left)/24,this.y_ratio=(this.gmp_container.clientHeight-this.container_padding_top-this.container_padding_bottom)/60,this.fill_poly(this,this.point_adjust(i.age,0),this.point_adjust(i.base,e),this.point_adjust(i.z3m,e),"none","#F4A65F",1),this.fill_poly(this,this.point_adjust(i.age,0),this.point_adjust(i.z2m,e),this.point_adjust(i.z3m,e),"none","#F0CF23",1),this.fill_poly(this,this.point_adjust(i.age,0),this.point_adjust(i.z2m,e),this.point_adjust(i.z1m,e),"none","#F4ED64",1),this.fill_poly(this,this.point_adjust(i.age,0),this.point_adjust(i.z1m,e),this.point_adjust(i.z2p,e),"none","#9FD193",1),this.draw_grid(0,0,24,60,"#858484",0,40);var _=this.generate_points_of_axis_json(i.age,i.base),s=this.base_pont_adjust(_,0,e);this.draw_poly_line(s,1,"black"),_=this.generate_points_of_axis_json(i.age,i.z3m);var s=this.base_pont_adjust(_,0,e);this.draw_poly_line(s,1,"black"),_=this.generate_points_of_axis_json(i.age,i.median);var s=this.base_pont_adjust(_,0,e);this.draw_poly_line(s,1,"black"),_=this.generate_points_of_axis_json(i.age,i.z2p);var s=this.base_pont_adjust(_,0,e);this.draw_poly_line(s,1,"black"),_=this.generate_points_of_axis_json(i.age,i.z1p);var s=this.base_pont_adjust(_,0,e);this.draw_poly_line(s,1,"black"),_=this.generate_points_of_axis_json(i.age,i.z1m);var s=this.base_pont_adjust(_,0,e);this.draw_poly_line(s,1,"black"),_=this.generate_points_of_axis_json(i.age,i.z3p);var s=this.base_pont_adjust(_,0,e);this.draw_poly_line(s,1,"black"),_=this.generate_points_of_axis_json(i.age,i.z2m);var s=this.base_pont_adjust(_,0,e);this.draw_poly_line(s,1,"black");var n=0;i.age.forEach(function(t){console.log(t),a.draw_circle(a.get_x(t),a.get_y(i.base[n]-e),2,1,"black","","base-point"),a.draw_circle(a.get_x(t),a.get_y(i.median[n]-e),2,1,"black","","base-point"),a.draw_circle(a.get_x(t),a.get_y(i.z1m[n]-e),2,1,"black","","base-point"),a.draw_circle(a.get_x(t),a.get_y(i.z1p[n]-e),2,1,"black","","base-point"),a.draw_circle(a.get_x(t),a.get_y(i.z2m[n]-e),2,1,"black","","base-point"),a.draw_circle(a.get_x(t),a.get_y(i.z2p[n]-e),2,1,"black","","base-point"),a.draw_circle(a.get_x(t),a.get_y(i.z3m[n]-e),2,1,"black","","base-point"),a.draw_circle(a.get_x(t),a.get_y(i.z3p[n]-e),2,1,"black","","base-point"),n++}),this.draw_poly_line(this.base_pont_adjust(t,0,40),2,"red","none"),this.legend(),this.chart_title("মেয়ে শিশুর উচ্চতা বৃদ্ধির চার্ট (০ - ২৪ মাস)",180,30),this.legend_text("বয়স (মাস)",340,470),this.legend_text("উচ্চতা বৃদ্ধি (সে. মি.)",30,260,-90)}girl_height_gain_chart_24_60(t=""){let a=this;var i=this.girl_height_24_60_chart_data,e=60;this.x_ratio=(this.gmp_container.clientWidth-this.container_padding_right-this.container_padding_left)/36,this.y_ratio=(this.gmp_container.clientHeight-this.container_padding_top-this.container_padding_bottom)/60,this.fill_poly(this,this.point_adjust(i.age,24),this.point_adjust(i.base,e),this.point_adjust(i.z3m,e),"none","#F4A65F",1),this.fill_poly(this,this.point_adjust(i.age,24),this.point_adjust(i.z2m,e),this.point_adjust(i.z3m,e),"none","#F0CF23",1),this.fill_poly(this,this.point_adjust(i.age,24),this.point_adjust(i.z2m,e),this.point_adjust(i.z1m,e),"none","#F4ED64",1),this.fill_poly(this,this.point_adjust(i.age,24),this.point_adjust(i.z1m,e),this.point_adjust(i.z2p,e),"none","#9FD193",1),this.draw_grid(0,0,36,130,"#858484",24,60);var _=this.generate_points_of_axis_json(i.age,i.base),s=this.base_pont_adjust(_,24,e);this.draw_poly_line(s,1,"black"),_=this.generate_points_of_axis_json(i.age,i.z3m);var s=this.base_pont_adjust(_,24,e);this.draw_poly_line(s,1,"black"),_=this.generate_points_of_axis_json(i.age,i.median);var s=this.base_pont_adjust(_,24,e);this.draw_poly_line(s,1,"black"),_=this.generate_points_of_axis_json(i.age,i.z2p);var s=this.base_pont_adjust(_,24,e);this.draw_poly_line(s,1,"black"),_=this.generate_points_of_axis_json(i.age,i.z1p);var s=this.base_pont_adjust(_,24,e);this.draw_poly_line(s,1,"black"),_=this.generate_points_of_axis_json(i.age,i.z1m);var s=this.base_pont_adjust(_,24,e);this.draw_poly_line(s,1,"black"),_=this.generate_points_of_axis_json(i.age,i.z3p);var s=this.base_pont_adjust(_,24,e);this.draw_poly_line(s,1,"black"),_=this.generate_points_of_axis_json(i.age,i.z2m);var s=this.base_pont_adjust(_,24,e);this.draw_poly_line(s,1,"black");var n=0;i.age.forEach(function(t){console.log(t),a.draw_circle(a.get_x(t-24),a.get_y(i.base[n]-e),2,1,"black","","base-point"),a.draw_circle(a.get_x(t-24),a.get_y(i.median[n]-e),2,1,"black","","base-point"),a.draw_circle(a.get_x(t-24),a.get_y(i.z1m[n]-e),2,1,"black","","base-point"),a.draw_circle(a.get_x(t-24),a.get_y(i.z1p[n]-e),2,1,"black","","base-point"),a.draw_circle(a.get_x(t-24),a.get_y(i.z2m[n]-e),2,1,"black","","base-point"),a.draw_circle(a.get_x(t-24),a.get_y(i.z2p[n]-e),2,1,"black","","base-point"),a.draw_circle(a.get_x(t-24),a.get_y(i.z3m[n]-e),2,1,"black","","base-point"),a.draw_circle(a.get_x(t-24),a.get_y(i.z3p[n]-e),2,1,"black","","base-point"),n++}),this.draw_poly_line(this.base_pont_adjust(t,0,40),2,"red","none"),this.legend(),this.chart_title("মেয়ে শিশুর উচ্চতা বৃদ্ধির চার্ট (২৪ - ৬০ মাস)",150,50),this.legend_text("বয়স (মাস)",340,470),this.legend_text("উচ্চতা বৃদ্ধি (সে. মি.)",30,260,-90)}}