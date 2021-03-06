package gac.coolteamname.fundnominal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText mPortfolioName;
    private ImageButton mPortfolioConfirm;
    private ImageButton mPortfolioEdit;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ViewPagerAdapter mViewPagerAdapter;

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setStatusBarColor();

        setContentView(R.layout.activity_main);

        PollService.setServiceAlarm(this, true);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        /**
         * Creating Adapter and set that adapter to the ViewPager.
         * setSupportActionBar method takes the Toolbar and sets it as
         * the default ActionBar thus making the Toolbar work like a normal ActionBar.
         */
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Set the PortfolioName to the first fund's Portfolio name
        mPortfolioName = (EditText) findViewById(R.id.portfolio_edit_text);
        List<Fund> fundInits = FundPortfolio.get(this).getFunds();
        if (fundInits.size() > 0) {
            if (fundInits.get(0).getPortfolioName() != null) {
                mPortfolioName.setText(fundInits.get(0).getPortfolioName());
            }
        }

        mPortfolioName.addTextChangedListener(new TextWatcher() {
            String beforeChanged;
            String afterChanged;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                beforeChanged = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                afterChanged = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePortfolioName(beforeChanged, afterChanged);
                //setTitleTextSize();
            }
        });

        // Button to edit/confirm fund name change
        mPortfolioConfirm = (ImageButton) findViewById(R.id.portfolio_confirm_button);
        mPortfolioEdit = (ImageButton) findViewById(R.id.portfolio_edit_button);
        mPortfolioEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPortfolioName.requestFocus();
                mPortfolioName.setSelection(mPortfolioName.getText().length());
                showKeyboard(v);
            }
        });

        mPortfolioConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPortfolioName.clearFocus();
                hideKeyboard(v);
            }
        });

        // Change visibility of buttons when edit Text get in/out of focus
        mPortfolioName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mPortfolioConfirm.setVisibility(View.VISIBLE);
                    mPortfolioEdit.setVisibility(View.GONE);
                }
                else {
                    mPortfolioConfirm.setVisibility(View.GONE);
                    mPortfolioEdit.setVisibility(View.VISIBLE);
                }
            }
        });

        editTextClearFocus();
        setUpTabs();
    }

    /**
     * Method to change the color of the Status Bar.<br>
     * <I>Note: Only works for API versions 21 or higher as of now</I>
     */
    private void setStatusBarColor() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }
    }

    private void editTextClearFocus() {
        mPortfolioName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    mPortfolioName.clearFocus();
                    hideKeyboard(v);
                }
                return false;
            }
        });
    }

    public void hideKeyboard(View view){
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showKeyboard(View view){
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    /**
     * Update Portfolio Name for every currently displayed Funds
     * @param beforeChanged old portfolio name
     * @param afterChanged new portfolio name
     */
    private void updatePortfolioName(String beforeChanged, String afterChanged) {
        List<Fund> funds = FundPortfolio.get(this).getFunds();
        for (Fund fund : funds) {
            if (fund.getPortfolioName() == (null) ||
                    fund.getPortfolioName().equals(beforeChanged)) {
                fund.setPortfolioName(afterChanged);
                FundPortfolio.get(this).updateFund(fund);
            }
        }
    }

    /**
     * Method to add Tabs to the TabLayout.
     * How many tabs that are added is measured by the getCount().
     * The setText sets the Tab title text.
     */
    private void setUpTabs() {
        for (int i = 0; i < mViewPagerAdapter.getCount(); i++){
            mTabLayout.addTab(mTabLayout.newTab()
                    .setText(mViewPagerAdapter.getPageTitle(i)));
        }

        /**
        TabTextColor sets the color for the title of the tabs, passing a ColorStateList here makes
        tab change colors in different situations such as selected, active, inactive etc.
        TabIndicatorColor sets the color for the indicator below the tabs
         */
        mTabLayout.setTabTextColors(ContextCompat.getColorStateList(this, R.color.tab_selector));
        mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.indicator));

        /**
        Adding a onPageChangeListener to the viewPager
        addOnPageChangeListener pass a TabLayoutPageChangeListener so that Tab Selection
        changes when a ViewPager page changes.
         For example: Makes the selection bar under the Tab slide over to the selected Tab
         */
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        /**
         * An onTabSelectedListener which makes Tabs clickable
         * and sets the ViewPager's layout to the selected Tab.
         * Without this clicking on a Tab does not change the layout, only swiping the screen will
         */
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (getIntent().getExtras() != null) {
            FundListFragment.mAutoUpdateFlag = true;
            mViewPager.setCurrentItem(getIntent().getExtras().getInt("tab_open_1"));
        }
    }


    /**
     * The ViewPager's adapter which handles what Fragment should be displayed by the ViewPager
     * for a given position.
     */
    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * @param position
         * Which Fragment to select <br>
         * 0: FundListFragment <br>
         * 1: ComparisonFragment
         * @return
         * New Fragment from selection
         */
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new FundListFragment();
                case 1:
                    return new ComparisonFragment();
                default:
                    return new FundListFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return "Portfolio";
                case 1:
                    return "Exchanges";
                default:
                    return "Default Text";
            }
        }

        /**
         * @return
         * The number of Tabs
         **/
        @Override
        public int getCount() {
            return 2;
        }

    }
}