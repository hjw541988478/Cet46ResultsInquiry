package me.hjw.cha46.activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private EditText et_id, et_name;
	private Button btn_find;
	private TextView tv_result;
	private char level;
	private static final String POST_URL = "http://cet.99sushe.com/find";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViews();

	}

	private void initViews() {
		et_id = (EditText) findViewById(R.id.et_id);
		et_name = (EditText) findViewById(R.id.et_name);
		btn_find = (Button) findViewById(R.id.btn_find);
		tv_result = (TextView) findViewById(R.id.tv_result);

		btn_find.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		String s_id = et_id.getText().toString();
		String s_name = et_name.getText().toString();

		if (s_id.length() != 15) {
			Toast.makeText(MainActivity.this, "准考证号只能为15位", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (TextUtils.isEmpty(s_name)) {
			Toast.makeText(MainActivity.this, "姓名不能为空", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		new GetScoreTask().execute(s_id, s_name);
	}

	public static String cha46(Map<String, String> map, String encode) {
		StringBuffer buffer = new StringBuffer();
		InputStream is = null;
		OutputStream os = null;
		if (map != null && !map.isEmpty()) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				try {
					buffer.append(entry.getKey())
							.append("=")
							.append(URLEncoder.encode(entry.getValue(), encode))
							.append("&");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			buffer.deleteCharAt(buffer.length() - 1);
		}
		StringBuilder sb = new StringBuilder();
		HttpURLConnection con = null;
		try {
			URL url = new URL(POST_URL);
			if (url != null) {
				con = (HttpURLConnection) url.openConnection();
				con.setDoInput(true);
				con.setDoOutput(true);
				con.setRequestMethod("POST");
				con.setConnectTimeout(3000);
				con.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:19.0) Gecko/20100101 Firefox/19.0");
				con.setRequestProperty("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
				con.setRequestProperty("Accept-Language",
						"zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
				con.setRequestProperty("Accept-Encoding", "gzip, deflate");
				con.setRequestProperty("Referer", "http://cet.99sushe.com");
				con.setRequestProperty("Connection", "keep-alive");
				byte[] tdata = buffer.toString().getBytes();
				os = con.getOutputStream();
				os.write(tdata);
				os.close();
				if (con.getResponseCode() == 200) {
					is = con.getInputStream();

					if (is != null) {
						byte[] data = new byte[1024];
						try {
							while ((is.read(data)) != -1) {
								sb.append(new String(data, "gbk"));
							}
						} catch (IOException e) {
							return null;
						}
					}
				} else {
					return null;
				}
			}
		} catch (MalformedURLException e) {
			return null;
		} catch (IOException e) {
			return null;
		} finally {
			con.disconnect();
		}

		return sb.toString();
	}

	private class GetScoreTask extends AsyncTask<String, Void, String> {

		ProgressDialog pd = null;

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(MainActivity.this);
			pd.setMessage("查询中...");
			pd.setCancelable(false);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.show();
		};

		@Override
		protected String doInBackground(String... params) {
			String s_id = params[0].trim();
			level = s_id.charAt(9);
			String s_name = params[1].trim();
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", s_id);
			map.put("name", s_name);
			String is = cha46(map, "gbk");
			if (is != null) {
				return is;
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				String[] results = result.split(",");
				if (results.length == 7) {
					StringBuilder sb = new StringBuilder();
					sb.append("姓名：" + results[6]);
					sb.append("\n学校：" + results[5]);
					sb.append("\n类别：" + "英语" + (level == '1' ? "四" : "六") + "级");
					sb.append("\n总分：" + results[4]);
					sb.append("\n听力：" + results[1]);
					sb.append("\n阅读：" + results[2]);
					sb.append("\n写作：" + results[3]);
					tv_result.setText(sb.toString());
				} else {
					tv_result.setText("输入的准考证或者姓名有误");
				}
			} else {
				tv_result.setText("输入的准考证或者姓名有误");
			}
			pd.dismiss();
		};
	};
}